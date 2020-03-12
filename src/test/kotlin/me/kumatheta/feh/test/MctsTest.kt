package me.kumatheta.feh.test

import kotlinx.coroutines.ExperimentalCoroutinesApi
import me.kumatheta.feh.*
import me.kumatheta.feh.mcts.*
import me.kumatheta.mcts.*
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.ExperimentalTime
import kotlin.time.MonoClock

@ExperimentalCoroutinesApi
@ExperimentalTime
fun main() {
    val dataSet = "duma infernal"
    Paths.get("data/$dataSet")
    val positionMap = readMap(Paths.get("data/$dataSet/$dataSet - map.csv"))
    val (_, spawnMap) = readUnits(Paths.get("data/$dataSet/$dataSet - spawn.csv"))
    val (playerMap, _) = readUnits(Paths.get("data/$dataSet/$dataSet - players.csv"))
    val battleMap = BasicBattleMap(
        positionMap,
        spawnMap,
        playerMap
    )
    val state = BattleState(battleMap)
    val phaseLimit = 20
    var board = newFehBoard(phaseLimit, state, 3, false, calculateScore = BattleState::calculateHeroBattleScore)
    val testMoves = listOf(
//        Rearrange(listOf(1, 4, 2, 3)),
        NormalMove(MoveAndBreak(heroUnitId = 3, moveTargetX = 4, moveTargetY = 2, obstacleX = 3, obstacleY = 1)),
        NormalMove(MoveAndBreak(heroUnitId = 2, moveTargetX = 2, moveTargetY = 1, obstacleX = 3, obstacleY = 1)),
        NormalMove(MoveAndAssist(heroUnitId = 4, moveTargetX = 4, moveTargetY = 1, assistTargetId = 3)),
        NormalMove(MoveAndAttack(heroUnitId = 3, moveTargetX = 4, moveTargetY = 4, attackTargetId = 8)),
        NormalMove(MoveOnly(heroUnitId = 1, moveTargetX = 1, moveTargetY = 2)),

        NormalMove(MoveAndAttack(heroUnitId = 3, moveTargetX = 3, moveTargetY = 3, attackTargetId = 10)),
        NormalMove(MoveAndAssist(heroUnitId = 4, moveTargetX = 3, moveTargetY = 2, assistTargetId = 3)),
        NormalMove(MoveAndAttack(heroUnitId = 3, moveTargetX = 3, moveTargetY = 1, attackTargetId = 10)),
        NormalMove(MoveAndAttack(heroUnitId = 2, moveTargetX = 4, moveTargetY = 1, attackTargetId = 10)),
        NormalMove(MoveOnly(heroUnitId = 1, moveTargetX = 1, moveTargetY = 2)),

        NormalMove(MoveOnly(heroUnitId = 2, moveTargetX = 2, moveTargetY = 1)),
        NormalMove(MoveAndAssist(heroUnitId = 4, moveTargetX = 1, moveTargetY = 1, assistTargetId = 2)),
        NormalMove(MoveAndAttack(heroUnitId = 3, moveTargetX = 3, moveTargetY = 2, attackTargetId = 9)),
        NormalMove(MoveAndAttack(heroUnitId = 2, moveTargetX = 2, moveTargetY = 2, attackTargetId = 6)),
        NormalMove(MoveOnly(heroUnitId = 1, moveTargetX = 0, moveTargetY = 2)),

        NormalMove(MoveOnly(heroUnitId = 1, moveTargetX = 1, moveTargetY = 2)),
        NormalMove(MoveOnly(heroUnitId = 3, moveTargetX = 2, moveTargetY = 1)),
        NormalMove(MoveAndAttack(heroUnitId = 2, moveTargetX = 0, moveTargetY = 2, attackTargetId = 11)),
        NormalMove(MoveAndAssist(heroUnitId = 4, moveTargetX = 1, moveTargetY = 1, assistTargetId = 1)),
        NormalMove(MoveOnly(heroUnitId = 1, moveTargetX = 1, moveTargetY = 2)),

        NormalMove(MoveAndAttack(heroUnitId = 3, moveTargetX = 2, moveTargetY = 1, attackTargetId = 16)),
        NormalMove(MoveAndAssist(heroUnitId = 4, moveTargetX = 1, moveTargetY = 1, assistTargetId = 3)),
        NormalMove(MoveOnly(heroUnitId = 2, moveTargetX = 2, moveTargetY = 2)),
        NormalMove(MoveOnly(heroUnitId = 1, moveTargetX = 0, moveTargetY = 2)),
        NormalMove(MoveAndAttack(heroUnitId = 3, moveTargetX = 1, moveTargetY = 2, attackTargetId = 16))

    )
//    val testResult = board.tryMoves(testMoves, false)
//    println("${testResult.enemyDied}, ${testResult.playerDied}, ${testResult.winningTeam}")
//
//    println(Json.stringify(UpdateInfo.serializer().list, toUpdateInfoList(board, testMoves).second))

    dumaMoveList.take(2).forEach { move ->
        val exists = board.moves.any {
            it == move
        }
        if (!exists) {
            throw IllegalStateException("moves not exists $move")
        }
        board = board.applyMove(move)
    }
    val scoreManager = LocalVaryingUCTTuned<FehMove>()
    val mcts = Mcts(board, scoreManager)
    var tries = 0
    val clockMark = MonoClock.markNow()
    var lastFixMove = MonoClock.markNow()


    repeat(10000) {
        mcts.run(5)
        val currentRootTries = mcts.rootScore.tries
        if (currentRootTries > 1000000 || mcts.estimatedSize > 650000 || lastFixMove.elapsedNow().inMinutes > 20) {
            mcts.moveDown()
            lastFixMove = MonoClock.markNow()
        }
        println("current root tries $currentRootTries")
        println("elapsed ${clockMark.elapsedNow()}")
        val score = mcts.score
        val bestMoves = score.moves ?: throw IllegalStateException()
        val testState = try {
            board.tryMoves(bestMoves)
        } catch (t: Throwable) {
            throw t
        }
        println(bestMoves)
        println("best score: ${score.bestScore}")
//        scoreManager.high = score.bestScore
//        scoreManager.average = score.totalScore / score.tries
        println("totalScore = ${score.totalScore}")
        println(
            "tries: ${score.tries - tries}, total tries: ${score.tries}, ${testState.enemyDied}, ${testState.playerDied}, ${testState.winningTeam}"
        )
        tries = score.tries
        println("estimatedSize: ${mcts.estimatedSize}")
        println("elapsed ${clockMark.elapsedNow()}")
//        if (clockMark.elapsedNow().inSeconds > 100) {
//            Runtime.getRuntime().gc()
//            println("memory used ${(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000_000}")
//            return
//        }
        println("memory used ${(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000_000}")
        if(testState.winningTeam == Team.PLAYER) {
            return
        }

    }
}

private fun testCreate(
    testRecycleManager: RecycleManager<FehMove, UCTScore<FehMove>>,
    board: FehBoard,
    scoreManager: VaryingUCT<FehMove>
): RecyclableNode<FehMove, UCTScore<FehMove>> {
    val recyclableNode = RecyclableNode(
        testRecycleManager,
        board,
        null,
        null,
        AtomicReference(scoreManager.newEmptyScore()),
        0
    )
    testRecycleManager.getDelegateNode(
        recyclableNode
    )
    return recyclableNode
}


