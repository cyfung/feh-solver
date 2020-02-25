package me.kumatheta.ws

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import me.kumatheta.feh.Axe
import me.kumatheta.feh.BasicBattleMap
import me.kumatheta.feh.BattleState
import me.kumatheta.feh.Beast
import me.kumatheta.feh.Bow
import me.kumatheta.feh.Color
import me.kumatheta.feh.Dagger
import me.kumatheta.feh.Dragon
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Lance
import me.kumatheta.feh.Magic
import me.kumatheta.feh.MoveAndAssist
import me.kumatheta.feh.MoveAndAttack
import me.kumatheta.feh.MoveAndBreak
import me.kumatheta.feh.MoveOnly
import me.kumatheta.feh.MoveType
import me.kumatheta.feh.PositionMap
import me.kumatheta.feh.Staff
import me.kumatheta.feh.Sword
import me.kumatheta.feh.Team
import me.kumatheta.feh.Terrain
import me.kumatheta.feh.UnitAction
import me.kumatheta.feh.WeaponType
import me.kumatheta.feh.mcts.FehBoard
import me.kumatheta.feh.mcts.FehMove
import me.kumatheta.feh.message.Action
import me.kumatheta.feh.message.AttackType
import me.kumatheta.feh.message.BattleMapPosition
import me.kumatheta.feh.message.MoveSet
import me.kumatheta.feh.message.SetupInfo
import me.kumatheta.feh.message.UnitAdded
import me.kumatheta.feh.message.UnitUpdate
import me.kumatheta.feh.message.UpdateInfo
import me.kumatheta.feh.readMap
import me.kumatheta.feh.readUnits
import me.kumatheta.mcts.Mcts
import me.kumatheta.mcts.VaryingUCT
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.ExperimentalTime
import kotlin.time.MonoClock

typealias MsgTerrain = me.kumatheta.feh.message.Terrain
typealias MsgBattleMap = me.kumatheta.feh.message.BattleMap
typealias MsgMoveType = me.kumatheta.feh.message.MoveType

private fun Terrain.toMsgTerrain(): MsgTerrain {
    val msgType = when (type) {
        Terrain.Type.WALL -> me.kumatheta.feh.message.Terrain.Type.WALL
        Terrain.Type.FLIER_ONLY -> me.kumatheta.feh.message.Terrain.Type.FLIER_ONLY
        Terrain.Type.FOREST -> me.kumatheta.feh.message.Terrain.Type.FOREST
        Terrain.Type.TRENCH -> me.kumatheta.feh.message.Terrain.Type.TRENCH
        Terrain.Type.REGULAR -> me.kumatheta.feh.message.Terrain.Type.REGULAR
    }
    return MsgTerrain(type = msgType, isDefenseTile = isDefenseTile)
}

private fun MoveType.toMsgMoveType(): MsgMoveType {
    return when (this) {
        MoveType.INFANTRY -> MsgMoveType.INFANTRY
        MoveType.ARMORED -> MsgMoveType.ARMORED
        MoveType.CAVALRY -> MsgMoveType.CAVALRY
        MoveType.FLYING -> MsgMoveType.FLIER
    }
}

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@ExperimentalTime
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    val positionMap = readMap(Paths.get("data/map.csv"))
    val (_, spawnMap) = readUnits(Paths.get("data/spawn.csv"))
    val (playerMap, _) = readUnits(Paths.get("data/players.csv"))
    val battleMap = BasicBattleMap(
        positionMap,
        spawnMap,
        playerMap
    )
    val state = BattleState(
        battleMap
    )
    val setupInfo = buildSetupInfo(positionMap, battleMap, state)
    val json = Json(JsonConfiguration.Stable)
    val mctsRef =
        AtomicReference<Pair<FehBoard, Mcts<FehMove, VaryingUCT.MyScore<FehMove>>>?>(null)
    val jobRef = AtomicReference<Job?>(null)

    routing {
        get("/start") {
            val (board, mcts) = getMcts(state, mctsRef, jobRef)
            call.respondText(json.stringify(SetupInfo.serializer(), setupInfo))
        }
        get("/moveset") {
            val isCompleted = jobRef.get() == null
            val pair = if (isCompleted) {
                // last get
                mctsRef.getAndSet(null)
            } else {
                mctsRef.get()
            }
            if (pair == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val (board, mcts) = pair
            val score = if (isCompleted) {
                mcts.score
            } else {
                resetScoreWithRetry(mcts)
            }
            val moves = score.moves
            if (moves == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            var lastState = board.stateCopy
            val details = board.tryAndGetDetails(moves)
            val updates = details.map { (unitAction, state) ->
                val action = unitAction?.toMsgAction()
                val oldUnits = (lastState.unitsSeq(Team.PLAYER) + lastState.unitsSeq(Team.ENEMY)).associateBy { it.id }
                val newUnits = (state.unitsSeq(Team.PLAYER) + state.unitsSeq(Team.ENEMY)).associateBy { it.id }
                val unitsUpdated = getUpdated(oldUnits, newUnits).toList()
                val unitsAdded =
                    newUnits.values.asSequence().filterNot { oldUnits.containsKey(it.id) }.map(HeroUnit::toUnitAdded)
                        .toList()
                lastState = state
                UpdateInfo(action, unitsUpdated, unitsAdded)
            }
            call.respondText(json.stringify(MoveSet.serializer(), MoveSet(updates, score.bestScore, score.tries)))
        }
    }
}

@ExperimentalTime
private suspend fun getMcts(
    state: BattleState,
    mctsRef: AtomicReference<Pair<FehBoard, Mcts<FehMove, VaryingUCT.MyScore<FehMove>>>?>,
    jobRef: AtomicReference<Job?>
): Pair<FehBoard, Mcts<FehMove, VaryingUCT.MyScore<FehMove>>> {
    val phraseLimit = 20
    val board = FehBoard(phraseLimit, state, 3)
    val scoreManager = VaryingUCT<FehMove>(3000, 2000, 0.5)
    val mcts = Mcts(board, scoreManager)
    val next = Pair(board, mcts)
    do {
        val prev = mctsRef.get()
        if (prev != null) {
            return prev
        }
    } while (!mctsRef.compareAndSet(null, next))

    val job = GlobalScope.launch {
        runMcts(mcts, board, scoreManager)
    }
    check(jobRef.compareAndSet(null, job))
    job.invokeOnCompletion {
        check(jobRef.compareAndSet(job, null))
    }
    return next
}

private fun getUpdated(
    oldUnits: Map<Int, HeroUnit>,
    newUnits: Map<Int, HeroUnit>
): Sequence<UnitUpdate> {
    return oldUnits.values.asSequence().mapNotNull { old ->
        val new = newUnits[old.id]
        if (new == null) {
            UnitUpdate(old.id, 0, false, 0, 0)
        } else {
            if (new.currentHp == old.currentHp && new.available == old.available && new.position == old.position) {
                null
            } else {
                UnitUpdate(old.id, new.currentHp, new.available, new.position.x, new.position.y)
            }
        }
    }
}

private fun UnitAction.toMsgAction(): Action {
    return when (this) {
        is MoveOnly -> Action(heroUnitId, moveTarget.x, moveTarget.y, null, null, null)
        is MoveAndAttack -> Action(heroUnitId, moveTarget.x, moveTarget.y, attackTargetId, null, null)
        is MoveAndBreak -> Action(heroUnitId, moveTarget.x, moveTarget.y, null, obstacle.x, obstacle.y)
        is MoveAndAssist -> Action(heroUnitId, moveTarget.x, moveTarget.y, assistTargetId, null, null)
    }
}

private suspend fun resetScoreWithRetry(mcts: Mcts<FehMove, VaryingUCT.MyScore<FehMove>>): VaryingUCT.MyScore<FehMove> {
    val score = mcts.resetRecentScore()
    val moves = score.moves
    if (moves == null) {
        delay(500)
        return mcts.resetRecentScore()
    }
    return score
}

@ExperimentalTime
private suspend fun runMcts(
    mcts: Mcts<FehMove, VaryingUCT.MyScore<FehMove>>,
    board: FehBoard,
    scoreManager: VaryingUCT<FehMove>
) {
    var tries = 0
    val fixedMoves = mutableListOf<FehMove>()
    val mctsStart = MonoClock.markNow()
    var lastFixMove = MonoClock.markNow()

    repeat(10000) {
        mcts.run(5)
        if (mcts.estimatedSize > 680000 || lastFixMove.elapsedNow().inMinutes > 20) {
            mcts.moveDown()
            lastFixMove = MonoClock.markNow()
        }
        println("elapsed ${mctsStart.elapsedNow()}")
        val score = mcts.score
        val bestMoves = score.moves ?: throw IllegalStateException()
        val testState = try {
            board.tryMoves(bestMoves)
        } catch (t: Throwable) {
            throw t
        }
        println("fixed:")
        fixedMoves.forEach {
            println(it)
        }
        println("changing:")
        bestMoves.forEach {
            println(it)
        }
        println("best score: ${score.bestScore}")
        scoreManager.high = score.bestScore
        scoreManager.average = score.totalScore / score.tries
        println("average = ${scoreManager.average}")
        println("calculated best score: ${board.calculateScore(testState)}")
        println("tries: ${score.tries - tries}, total tries: ${score.tries}, ${testState.enemyDied}, ${testState.playerDied}, ${testState.winningTeam}")
        tries = score.tries
        println("estimatedSize: ${mcts.estimatedSize}")
        println("elapsed ${mctsStart.elapsedNow()}")
        println(
            "memory used ${(Runtime.getRuntime().totalMemory() - Runtime.getRuntime()
                .freeMemory()) / 1000_000}"
        )
        if (testState.enemyCount == testState.enemyDied && testState.playerDied == 0) {
            return
        }
    }
}

private fun buildSetupInfo(
    positionMap: PositionMap,
    battleMap: BasicBattleMap,
    state: BattleState
): SetupInfo {
    val battleMapPositions = positionMap.terrainMap.map { (position, terrain) ->
        BattleMapPosition(
            x = position.x,
            y = position.y,
            terrain = terrain.toMsgTerrain(),
            obstacle = positionMap.obstacles[position] ?: 0
        )
    }
    val msgBattleMap = MsgBattleMap(
        sizeX = battleMap.size.x,
        sizeY = battleMap.size.y,
        battleMapPositions = battleMapPositions
    )
    val unitsAdded = (state.unitsSeq(Team.PLAYER) + state.unitsSeq(Team.ENEMY)).map {
        it.toUnitAdded()
    }.toList()
    return SetupInfo(msgBattleMap, unitsAdded)
}

private fun HeroUnit.toUnitAdded(): UnitAdded {
    check(currentHp == stat.hp)
    return UnitAdded(
        name = name,
        unitId = id,
        maxHp = stat.hp,
        playerControl = team == Team.PLAYER,
        startX = position.x,
        startY = position.y,
        moveType = moveType.toMsgMoveType(),
        attackType = weaponType.toAttackType()
    )
}

private fun WeaponType.toAttackType(): AttackType {
    return when (this) {
        is Dagger -> when (color) {
            Color.RED -> AttackType.RED_DAGGER
            Color.GREEN -> AttackType.GREEN_DAGGER
            Color.BLUE -> AttackType.BLUE_DAGGER
            Color.COLORLESS -> AttackType.COLORLESS_DAGGER
        }
        is Bow -> when (color) {
            Color.RED -> AttackType.RED_BOW
            Color.GREEN -> AttackType.GREEN_BOW
            Color.BLUE -> AttackType.BLUE_BOW
            Color.COLORLESS -> AttackType.COLORLESS_BOW
        }
        is Beast -> when (color) {
            Color.RED -> AttackType.RED_BEAST
            Color.GREEN -> AttackType.GREEN_BEAST
            Color.BLUE -> AttackType.BLUE_BEAST
            Color.COLORLESS -> AttackType.COLORLESS_BEAST
        }
        is Dragon -> when (color) {
            Color.RED -> AttackType.RED_BREATH
            Color.GREEN -> AttackType.GREEN_BREATH
            Color.BLUE -> AttackType.BLUE_BREATH
            Color.COLORLESS -> AttackType.COLORLESS_BREATH
        }
        Staff -> AttackType.STAFF
        Sword -> AttackType.SWORD
        Lance -> AttackType.LANCE
        Axe -> AttackType.AXE
        is Magic -> when (color) {
            Color.RED -> AttackType.RED_TOME
            Color.GREEN -> AttackType.GREEN_TOME
            Color.BLUE -> AttackType.BLUE_TOME
            Color.COLORLESS -> throw IllegalArgumentException("no colorless tome")
        }
    }
}

