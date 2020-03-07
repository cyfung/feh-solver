package me.kumatheta.ws

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.put
import io.ktor.routing.routing
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import kotlinx.serialization.protobuf.ProtoBuf
import me.kumatheta.feh.*
import me.kumatheta.feh.MoveType
import me.kumatheta.feh.Terrain
import me.kumatheta.feh.mcts.*
import me.kumatheta.feh.message.*
import me.kumatheta.mcts.Mcts
import me.kumatheta.mcts.VaryingUCT
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.ClockMark
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

@ExperimentalCoroutinesApi
@ExperimentalTime
@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    val protoBuf = ProtoBuf()
    val jobInfoRef = AtomicReference<JobInfo?>(null)

    routing {
        get("/job") {
            val jobInfo = getOrStartJob(JobConfig(), jobInfoRef)
            call.respond(protoBuf.dump(SetupInfo.serializer(), jobInfo.setupInfo))
        }
        put("/job") {
            val jobInfo = restartJob(JobConfig(), jobInfoRef)
            call.respond(protoBuf.dump(SetupInfo.serializer(), jobInfo.setupInfo))
        }
        delete("/job") {
            jobInfoRef.get()?.completableJob?.cancel()
            call.respond("cancel success")
        }
        get("/job/moveSet") {
            val jobInfo = jobInfoRef.get()
            if (jobInfo == null) {
                call.respond(HttpStatusCode.BadRequest, "no job running")
                return@get
            }
            val isCompleted = jobInfo.completableJob.isCompleted
            val (_, board, mcts) = jobInfo
            val currentScore = if (isCompleted) {
                mcts.score
            } else {
                resetScoreWithRetry(mcts)
            }
            val elapsed: Long = if(isCompleted) {
                jobInfo.elapsed.get()
            } else {
                jobInfo.startTime.elapsedNow().toLong(TimeUnit.SECONDS)
            }
            val moves = currentScore.moves
            if (moves == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val (testState, updates) = toUpdateInfoList(board, moves)

            val allTimeBest = mcts.score
            val runtime = Runtime.getRuntime()
            val moveSet = MoveSet(
                updates,
                currentScore.bestScore,
                currentScore.tries,
                testState.enemyDied,
                testState.playerDied,
                allTimeBest.bestScore,
                allTimeBest.tries,
                mcts.estimatedSize,
                runtime.totalMemory() - runtime.freeMemory(),
                elapsed,
                isCompleted
            )

            call.respond(protoBuf.dump(MoveSet.serializer(), moveSet))
        }
    }
}

private val NULL_ACTION = Action(-1, -1, -1, -1, -1, -1)

fun toUpdateInfoList(
    board: FehBoard,
    moves: List<FehMove>
): Pair<BattleState, List<UpdateInfo>> {
    var lastState = board.getStateCopy()
    val details = board.tryAndGetDetails(moves)
    val list = details.map { (unitAction, state) ->
        val action = unitAction?.toMsgAction()
        val oldUnits = (lastState.unitsSeq(Team.PLAYER) + lastState.unitsSeq(Team.ENEMY)).associateBy { it.id }
        val newUnits = (state.unitsSeq(Team.PLAYER) + state.unitsSeq(Team.ENEMY)).associateBy { it.id }
        val unitsUpdated = getUpdated(oldUnits, newUnits).toList()
        val unitsAdded =
            newUnits.values.asSequence().filterNot { oldUnits.containsKey(it.id) }.map(HeroUnit::toUnitAdded)
                .toList()
        lastState = state
        UpdateInfo(action ?: NULL_ACTION, unitsUpdated, unitsAdded)
    }
    return lastState to list
}

data class JobConfig(
    val mapName: String = "duma infernal",
    val phaseLimit: Int = 20,
    val explorationConstantC: Double = 1.5,
    val maxTurnBeforeEngage: Int = 3
)

@ExperimentalCoroutinesApi
@ExperimentalTime
private fun getOrStartJob(
    jobConfig: JobConfig,
    jobInfoRef: AtomicReference<JobInfo?>
): JobInfo {
    val jobInfo = jobInfoRef.get()
    if (jobInfo != null) {
        return jobInfo
    }
    val newJobInfo = newJobInfo(jobConfig)
    do {
        val prev = jobInfoRef.get()
        if (prev != null) {
            return prev
        }
    } while (!jobInfoRef.compareAndSet(null, newJobInfo))

    startNewJob(newJobInfo)
    return newJobInfo
}

@ExperimentalCoroutinesApi
@ExperimentalTime
private fun restartJob(
    jobConfig: JobConfig,
    jobInfoRef: AtomicReference<JobInfo?>
): JobInfo {
    val newJobInfo = newJobInfo(jobConfig)
    val oldJobInfo = jobInfoRef.getAndSet(newJobInfo)
    oldJobInfo?.completableJob?.cancel()

    startNewJob(newJobInfo)
    return newJobInfo
}

@ExperimentalCoroutinesApi
@ExperimentalTime
private fun startNewJob(newJobInfo: JobInfo) {
    val job = GlobalScope.launch {
        runMcts(newJobInfo)
    }
    job.invokeOnCompletion {
        newJobInfo.elapsed.set(newJobInfo.startTime.elapsedNow().toLong(TimeUnit.SECONDS))
        newJobInfo.completableJob.complete()
    }
    newJobInfo.completableJob.invokeOnCompletion {
        job.cancel()
    }
}

@ExperimentalTime
@ExperimentalCoroutinesApi
private fun newJobInfo(
    jobConfig: JobConfig
): JobInfo {
    val (mapName, phaseLimit, explorationConstantC, maxTurnBeforeEngage) = jobConfig
    Paths.get("data/$mapName")
    val positionMap = readMap(Paths.get("data/$mapName/$mapName - map.csv"))
    val (_, spawnMap) = readUnits(Paths.get("data/$mapName/$mapName - spawn.csv"))
    val (playerMap, _) = readUnits(Paths.get("data/$mapName/$mapName - players.csv"))
    val battleMap = BasicBattleMap(
        positionMap,
        spawnMap,
        playerMap
    )
    val state = BattleState(
        battleMap,
        false
    )
    val setupInfo = buildSetupInfo(positionMap, battleMap, state)
    val board = newFehBoard(phaseLimit, state, maxTurnBeforeEngage)
    val scoreManager = VaryingUCT<FehMove>(3000, 2000, explorationConstantC)
    val mcts = Mcts(board, scoreManager)
    return JobInfo(setupInfo, board, mcts, Job())
}

@ExperimentalTime
@ExperimentalCoroutinesApi
data class JobInfo(
    val setupInfo: SetupInfo,
    val board: FehBoard,
    val mcts: Mcts<FehMove, VaryingUCT.MyScore<FehMove>, VaryingUCT<FehMove>>,
    val completableJob: CompletableJob,
    val startTime: ClockMark = MonoClock.markNow(),
    val elapsed: AtomicLong = AtomicLong()
)

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
        is MoveOnly -> Action(heroUnitId, moveTarget.x, moveTarget.y, -1, -1, -1)
        is MoveAndAttack -> Action(heroUnitId, moveTarget.x, moveTarget.y, attackTargetId, -1, -1)
        is MoveAndBreak -> Action(heroUnitId, moveTarget.x, moveTarget.y, -1, obstacle.x, obstacle.y)
        is MoveAndAssist -> Action(heroUnitId, moveTarget.x, moveTarget.y, assistTargetId, -1, -1)
    }
}

@ExperimentalCoroutinesApi
private suspend fun resetScoreWithRetry(mcts: Mcts<FehMove, VaryingUCT.MyScore<FehMove>, VaryingUCT<FehMove>>): VaryingUCT.MyScore<FehMove> {
    repeat(10) {
        val score = mcts.resetRecentScore()
        val moves = score.moves
        if (moves != null) {
            return score
        }
        println("delay response")
        delay(50)
    }
    return mcts.resetRecentScore()
}

@ExperimentalCoroutinesApi
@ExperimentalTime
private suspend fun runMcts(
    jobInfo: JobInfo
) = coroutineScope {
    val mcts = jobInfo.mcts
    val board = jobInfo.board
    var tries = 0
    val mctsStart = MonoClock.markNow()
    var lastFixMove = MonoClock.markNow()

    val json = Json(JsonConfiguration.Stable)

    repeat(10000) {
        if (!isActive) {
            return@coroutineScope
        }
        mcts.run(5, parallelCount = 5)
        if (mcts.estimatedSize > 680000 || lastFixMove.elapsedNow().inMinutes > 20) {
            mcts.moveDown()
            lastFixMove = MonoClock.markNow()
        }
        println("elapsed ${mctsStart.elapsedNow()}")
        val score = mcts.score
        val bestMoves = score.moves ?: throw IllegalStateException()
        val testState = board.tryMoves(bestMoves)

        println(bestMoves)
        println(json.stringify(UpdateInfo.serializer().list, toUpdateInfoList(board, bestMoves).second))

        println("best score: ${score.bestScore}")
        mcts.scoreManager.high = score.bestScore
        mcts.scoreManager.average = score.totalScore / score.tries
        println("average = ${mcts.scoreManager.average}")
        println("tries: ${score.tries - tries}, total tries: ${score.tries}, ${testState.enemyDied}, ${testState.playerDied}, ${testState.winningTeam}")
        tries = score.tries
        println("estimatedSize: ${mcts.estimatedSize}")
        println("elapsed ${mctsStart.elapsedNow()}")
        println(
            "memory used ${(Runtime.getRuntime().totalMemory() - Runtime.getRuntime()
                .freeMemory()) / 1000_000}"
        )
        if (testState.winningTeam == Team.PLAYER) {
            return@coroutineScope
        }
    }
}

private fun buildSetupInfo(
    positionMap: PositionMap,
    battleMap: BasicBattleMap,
    state: BattleState
): SetupInfo {
    val chessPieceMap = battleMap.toChessPieceMap()
    val battleMapPositions = positionMap.terrainMap.map { (position, terrain) ->
        BattleMapPosition(
            x = position.x,
            y = position.y,
            terrain = terrain.toMsgTerrain(),
            obstacle = (chessPieceMap[position] as? Obstacle)?.health ?: 0
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
    check(currentHp == maxHp)
    return UnitAdded(
        name = name,
        imageName = imageName,
        unitId = id,
        maxHp = maxHp,
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

