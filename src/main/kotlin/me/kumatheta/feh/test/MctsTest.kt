package me.kumatheta.feh.test

import me.kumatheta.feh.BasicBattleMap
import me.kumatheta.feh.BattleState
import me.kumatheta.feh.MoveAndAssist
import me.kumatheta.feh.MoveAndAttack
import me.kumatheta.feh.MoveOnly
import me.kumatheta.feh.Position
import me.kumatheta.feh.mcts.FehBoard
import me.kumatheta.feh.mcts.FehMove
import me.kumatheta.feh.readMap
import me.kumatheta.feh.readUnits
import me.kumatheta.mcts.Mcts
import me.kumatheta.mcts.RecyclableNode
import me.kumatheta.mcts.RecycleManager
import me.kumatheta.mcts.VaryingUCT
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.ExperimentalTime
import kotlin.time.MonoClock

@ExperimentalTime
fun main() {
    val dataSet = "bhb morgan morgan"
    Paths.get("data/$dataSet")
    val positionMap = readMap(Paths.get("data/$dataSet/$dataSet - map.csv"))
    val (_, spawnMap) = readUnits(Paths.get("data/$dataSet/$dataSet - spawn.csv"))
    val (playerMap, _) = readUnits(Paths.get("data/$dataSet/$dataSet - players.csv"))
    val state = BattleState(
        BasicBattleMap(
            positionMap,
            spawnMap,
            playerMap
        )
    )
    val phraseLimit = 20
    var board = FehBoard(phraseLimit, state, 3)
    val testMoves = listOf(
        MoveOnly(1, Position(0, 1)),
        MoveOnly(4, Position(1, 0)),
        MoveOnly(3, Position(2, 1)),
        MoveOnly(2, Position(5,1)),
        MoveAndAttack(3,Position(1,3), 8),
        MoveOnly(1, Position(1,1)),
        MoveAndAssist(4, Position(1,2), 3),
        MoveAndAssist(3, Position(0,2), 4),
        MoveOnly(2, Position(4,1)),

        MoveAndAttack(3,Position(3,2), 10),
        MoveAndAssist(4, Position(4,2), 3),
        MoveAndAttack(3,Position(1,2), 9),
        MoveAndAttack(2,Position(3,2), 5)
    ).map {
        FehMove(it)
    }
    val testResult = board.tryMoves(testMoves,true)
//    println("${testResult.enemyDied}, ${testResult.playerDied}, ${testResult.winningTeam}")
//
//    testMoves.take(9).forEach { move ->
//        val exists = board.moves.any {
//            it == move
//        }
//        if (!exists) {
//            throw IllegalStateException()
//        }
//        board = board.applyMove(move)
//    }
    val scoreManager = VaryingUCT<FehMove>(3000, 2000, 0.5)
    val mcts = Mcts(board, scoreManager)
    var tries = 0
    val fixedMoves = mutableListOf<FehMove>()
    val clockMark = MonoClock.markNow()
    var lastFixMove = MonoClock.markNow()


    repeat(10000) {
        mcts.run(5)
        if (mcts.estimatedSize > 680000 || lastFixMove.elapsedNow().inMinutes > 20) {
            mcts.moveDown()
            lastFixMove = MonoClock.markNow()
        }
        println("elapsed ${clockMark.elapsedNow()}")
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
        println("elapsed ${clockMark.elapsedNow()}")
//        if (clockMark.elapsedNow().inSeconds > 100) {
//            Runtime.getRuntime().gc()
//            println("memory used ${(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000_000}")
//            return
//        }
        println("memory used ${(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000_000}")
//        if (testState.enemyCount == testState.enemyDied && testState.playerDied == 0) {
//            return
//        }
    }
}

private fun testCreate(
    testRecycleManager: RecycleManager<FehMove, VaryingUCT.MyScore<FehMove>>,
    board: FehBoard,
    scoreManager: VaryingUCT<FehMove>
): RecyclableNode<FehMove, VaryingUCT.MyScore<FehMove>> {
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


