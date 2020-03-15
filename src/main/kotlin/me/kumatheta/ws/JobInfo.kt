package me.kumatheta.ws

import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import me.kumatheta.feh.*
import me.kumatheta.feh.MoveType
import me.kumatheta.feh.Terrain
import me.kumatheta.feh.mcts.FehBoard
import me.kumatheta.feh.mcts.FehMove
import me.kumatheta.feh.mcts.newFehBoard
import me.kumatheta.feh.mcts.tryMoves
import me.kumatheta.feh.message.*
import me.kumatheta.mcts.*
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.ClockMark
import kotlin.time.ExperimentalTime
import kotlin.time.MonoClock

@ExperimentalTime
@ExperimentalCoroutinesApi
data class FehJobInfo<out S : Score<FehMove>>(
    val jobConfig: FehJobConfig<out S, *>,
    val setupInfo: SetupInfo,
    val board: FehBoard,
    val mcts: Mcts<FehMove, S>,
    val completableJob: CompletableJob,
    val startTime: ClockMark = MonoClock.markNow(),
    val elapsed: AtomicLong = AtomicLong()
)

@ExperimentalTime
@ExperimentalCoroutinesApi
fun <S : Score<FehMove>, M : ScoreManagerFactory<FehMove, S>> FehJobConfig<S, M>.toJobInfo(): FehJobInfo<S> {
    Paths.get("data/$mapName")
    val positionMap = readMap(Paths.get("data/$mapName/$mapName - map.csv"))
    val (_, spawnMap) = readUnits(Paths.get("data/$mapName/$mapName - spawn.csv"))
    val (playerMap, _) = readUnits(Paths.get("data/$mapName/$mapName - players.csv"))
    val battleMap = BasicBattleMap(
        positionMap,
        spawnMap,
        playerMap
    )
    val state = BattleState(battleMap.toInternalBattleMap())
    val setupInfo = buildSetupInfo(positionMap, battleMap, state)
    var board = newFehBoard(
        phaseLimit,
        state,
        maxTurnBeforeEngage,
        canRearrange = canRearrange,
        toRating = toRating,
        calculateScore = calculateScore
    )
    startingMoves?.forEach {
        require(board.moves.contains(it))
        board = board.applyMove(it)
    }

    val mcts = Mcts(board, scoreManagerFactory)

    return FehJobInfo(
        jobConfig = this,
        setupInfo = setupInfo,
        board = board,
        mcts = mcts,
        completableJob = Job()
    )
}

@ExperimentalCoroutinesApi
@ExperimentalTime
fun <S : Score<FehMove>> FehJobInfo<S>.startNewJob() {
    val job = GlobalScope.launch {
        runMcts()
    }
    job.invokeOnCompletion {
        elapsed.set(startTime.elapsedNow().toLong(TimeUnit.SECONDS))
        completableJob.complete()
    }
    completableJob.invokeOnCompletion {
        job.cancel()
    }
}

@ExperimentalCoroutinesApi
@ExperimentalTime
private suspend fun <S : Score<FehMove>> FehJobInfo<S>.runMcts() =
    coroutineScope {
        val mcts = mcts
        val board = board
        var tries = 0
        val mctsStart = MonoClock.markNow()
        var lastFixMove = MonoClock.markNow()

        val json = Json(JsonConfiguration.Stable)

        repeat(10000) {
            if (!isActive) {
                return@coroutineScope
            }
            mcts.run(second = 5, parallelCount = jobConfig.parallelCount)
            val rootScore = mcts.rootScore
            println("current root tries: ${rootScore.tries}")
            if (rootScore.tries > 1000000 || mcts.estimatedSize > 680000 || lastFixMove.elapsedNow().inMinutes > 20) {
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
            if (jobConfig.scoreManagerFactory is ScoreRefRequired) {
                jobConfig.scoreManagerFactory.update(score.totalScore / score.tries, score.bestScore)
            }
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

private fun Terrain.toMsgTerrain(): MsgTerrain {
    val msgType = when (type) {
        Terrain.Type.WALL -> MsgTerrainType.WALL
        Terrain.Type.FLIER_ONLY -> MsgTerrainType.FLIER_ONLY
        Terrain.Type.FOREST -> MsgTerrainType.FOREST
        Terrain.Type.TRENCH -> MsgTerrainType.TRENCH
        Terrain.Type.REGULAR -> MsgTerrainType.REGULAR
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

fun HeroUnit.toUnitAdded(): UnitAdded {
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

