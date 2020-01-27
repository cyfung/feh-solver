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
import java.lang.IllegalStateException
import java.nio.file.Paths
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

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
    val board = FehBoard(phraseLimit, state)
    val testMoves = listOf(
        MoveOnly(2, Position(5, 6)),
        MoveOnly(3, Position(4, 4)),
        MoveOnly(1, Position(3, 5)),
        MoveOnly(4, Position(4, 5)),
        MoveAndAttack(2, Position(5, 4), 8),
        MoveOnly(1, Position(3, 5)),
        MoveAndAssist(4, Position(4, 5), 1),
        MoveAndAttack(1, Position(4, 3), 6),
        MoveOnly(3, Position(4, 4)),
        MoveAndAttack(3, Position(4, 2), 7),
        MoveAndAttack(2, Position(5, 5), 5),
        MoveAndAssist(4, Position(5, 6), 2),
        MoveAndAttack(2, Position(5, 5), 5),
        MoveOnly(1, Position(4, 1)),
        MoveAndAttack(2, Position(5, 5), 9)
    ).map {
        FehMove(it)
    }
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
    val mcts = Mcts(board, 1.0, 500000000.0)

//    val mcts = Mcts(board, 0.3, 500000000.0)
    var tries = 0
    repeat(1000) {
        mcts.run(5)
        val bestScore = mcts.bestScore
        val bestMoves = bestScore.moves ?: throw IllegalStateException()
        val testState = try {
            board.tryMoves(bestMoves)
        } catch (t: Throwable) {
            throw t
        }
        bestMoves.forEach {
            println(it)
        }
        println("best score: ${bestScore.bestScore}")
        println("calculated best score: ${board.calculateScore(testState)}")
        println("tries: ${bestScore.tries - tries}, total tries: ${bestScore.tries}, ${testState.enemyDied}, ${testState.playerDied}, ${testState.winningTeam}")
        tries = bestScore.tries
    }
}


