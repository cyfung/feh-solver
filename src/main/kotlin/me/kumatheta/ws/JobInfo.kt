package me.kumatheta.ws

import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
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
import me.kumatheta.feh.MoveType
import me.kumatheta.feh.Position
import me.kumatheta.feh.PositionMap
import me.kumatheta.feh.Staff
import me.kumatheta.feh.Sword
import me.kumatheta.feh.Team
import me.kumatheta.feh.Terrain
import me.kumatheta.feh.WeaponType
import me.kumatheta.feh.mcts.FehBoard
import me.kumatheta.feh.mcts.FehMove
import me.kumatheta.feh.mcts.newFehBoard
import me.kumatheta.feh.message.AttackType
import me.kumatheta.feh.message.BattleMapPosition
import me.kumatheta.feh.message.SetupInfo
import me.kumatheta.feh.message.UnitAdded
import me.kumatheta.feh.message.UpdateInfo
import me.kumatheta.feh.readMap
import me.kumatheta.feh.readUnits
import me.kumatheta.mcts.Mcts
import me.kumatheta.mcts.Score
import me.kumatheta.mcts.ScoreManagerFactory
import me.kumatheta.mcts.ScoreRefRequired
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
        spawnMap
    )
    val setupInfo = buildSetupInfo(positionMap, battleMap)
    var board = newFehBoard(
        phaseLimit,
        maxTurnBeforeEngage,
        canRearrange = canRearrange,
        toRating = toRating,
        calculateScore = calculateScore,
        stateBuilder = {
            BattleState(battleMap.toInternalBattleMap(it))
        },
        allPlayerUnits = playerMap.values.map { HeroUnit(0, it, Team.PLAYER, Position(0, 0)) },
        playerCount = positionMap.playerIds.size
    )
    startingMoves?.forEach {
        require(board.moves.contains(it)) {
            "$it not exists"
        }
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
        val moves = jobConfig.suggestedMoves?.toList()
        if (moves != null) {
            mcts.playOut(
                moves
            )
        }
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

        val moveDownCriteria = jobConfig.moveDownCriteria
        repeat(10000) {
            if (!isActive) {
                return@coroutineScope
            }
            mcts.run(second = 5, parallelCount = jobConfig.parallelCount)
            val rootScore = mcts.rootScore
            println("current root tries: ${rootScore.tries}")
            if (moveDownCriteria.maxTries != null && moveDownCriteria.maxTries < rootScore.tries ||
                moveDownCriteria.maxNodes != null && moveDownCriteria.maxNodes < mcts.estimatedSize ||
                moveDownCriteria.maxDuration != null && moveDownCriteria.maxDuration < lastFixMove.elapsedNow()
            ) {
                mcts.moveDown()
                lastFixMove = MonoClock.markNow()
            }
            println("elapsed ${mctsStart.elapsedNow()}, moveDownCount ${mcts.moveDownCount}")
            val score = mcts.score
            val bestMoves = score.moves ?: throw IllegalStateException()

            println(bestMoves)
            val (testState, updateInfoList) = toUpdateInfoList(board, bestMoves)
            println(json.stringify(UpdateInfo.serializer().list, updateInfoList))

            println("best score: ${score.bestScore}")
            if (jobConfig.scoreManagerFactory is ScoreRefRequired) {
                jobConfig.scoreManagerFactory.updateScoreRef(score.totalScore / score.tries, score.bestScore)
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
    battleMap: BasicBattleMap
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
    return SetupInfo(msgBattleMap)
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
            Color.COLORLESS -> AttackType.COLORLESS_TOME
        }
    }
}

