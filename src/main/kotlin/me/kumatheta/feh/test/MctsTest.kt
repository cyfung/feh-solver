package me.kumatheta.feh.test

import me.kumatheta.feh.BasicBattleMap
import me.kumatheta.feh.BattleState
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
//    val solver = BattleSolver(TestMap, 10)
//    solver.solve()

    val positionMap = readMap(Paths.get("test/feh - map.csv"))
    val (_, spawnMap) = readUnits(Paths.get("test/feh - spawn.csv"))
    val (playerMap, _) = readUnits(Paths.get("test/feh - players.csv"))
    val state = BattleState(
        BasicBattleMap(
            positionMap,
            spawnMap,
            playerMap
        )
    )
    val phraseLimit = 20
    var board = FehBoard(phraseLimit, state, 3)
//    val testMoves = listOf(
//        MoveOnly(2, Position(5, 6)),
//        MoveOnly(3, Position(4, 4)),
//        MoveOnly(1, Position(3, 5)),
//        MoveOnly(4, Position(4, 5)),
//        MoveAndAttack(2, Position(5, 4), 8),
//        MoveOnly(1, Position(3, 5)),
//        MoveAndAssist(4, Position(4, 5), 1),
//        MoveAndAttack(1, Position(4, 3), 6),
//        MoveOnly(3, Position(4, 4)),
//        MoveAndAttack(3, Position(4, 2), 7),
//        MoveAndAttack(2, Position(5, 5), 5),
//        MoveAndAssist(4, Position(5, 6), 2),
//        MoveAndAttack(2, Position(5, 5), 5),
//        MoveOnly(1, Position(4, 1)),
//        MoveAndAttack(2, Position(5, 5), 9)
//    ).map {
//        FehMove(it)
//    }
//    val testMoves = listOf(
//        MoveOnly(2, Position(5, 6)),
//        MoveOnly(3, Position(4, 4)),
//        MoveOnly(1, Position(3, 5)),
//        MoveOnly(4, Position(4, 5)),
//        MoveAndAttack(2, Position(5, 4), 8),
//        MoveOnly(1, Position(5,6)),
//        MoveOnly(4, Position(5,5)),
//        MoveOnly(3, Position(4, 2))
//    ).map {
//        FehMove(it)
//    }
//    val testResult = board.tryMoves(testMoves.take(4))
//    println("${testResult.enemyDied}, ${testResult.playerDied}, ${testResult.winningTeam}")
//
//    testMoves.take(4).forEach { move ->
//        val exists = board.moves.any {
//            it == move
//        }
//        if (!exists) {
//            throw IllegalStateException()
//        }
//        board.applyMove(move)
//    }
//    val scoreManager = ModifiedUCT<FehMove>(0.3, 500000000.0)
    val scoreManager = VaryingUCT<FehMove>(3000, 2000, 0.5)
    val mcts = Mcts(board, scoreManager)
//    val mcts = Mcts(board, 0.3, 500000000.0)
    var tries = 0
    val fixedMoves = mutableListOf<FehMove>()
    val clockMark = MonoClock.markNow()
    var lastFixMove = MonoClock.markNow()
    repeat(10000) {
        mcts.run(5)
        if (mcts.estimatedSize > 180000 || lastFixMove.elapsedNow().inMinutes > 20) {
            val move = mcts.moveDown()
            board = board.applyMove(move)
            fixedMoves.add(move)
            lastFixMove = MonoClock.markNow()
        }
        println("elapsed ${clockMark.elapsedNow()}")
        val bestScore = mcts.bestScore
        val bestMoves = bestScore.moves ?: throw IllegalStateException()
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
        println("best score: ${bestScore.bestScore}")
        scoreManager.high = bestScore.bestScore
        scoreManager.average = bestScore.totalScore / bestScore.tries
        println("average = ${scoreManager.average}")
        println("calculated best score: ${board.calculateScore(testState)}")
        println("tries: ${bestScore.tries - tries}, total tries: ${bestScore.tries}, ${testState.enemyDied}, ${testState.playerDied}, ${testState.winningTeam}")
        tries = bestScore.tries
        println("estimatedSize: ${mcts.estimatedSize}")
        println("elapsed ${clockMark.elapsedNow()}")
        println("memory used ${(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000_000}")
        if (testState.enemyCount == testState.enemyDied && testState.playerDied == 0) {
            return
        }
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
        0,
        false
    )
    testRecycleManager.getDelegateNode(
        recyclableNode
    )
    return recyclableNode
}


