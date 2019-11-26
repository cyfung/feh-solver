package me.kumatheta.feh.test

import me.kumatheta.feh.BasicBattleMap
import me.kumatheta.feh.BattleState
import me.kumatheta.feh.MoveOnly
import me.kumatheta.feh.Position
import me.kumatheta.feh.UnitAction
import me.kumatheta.feh.mcts.FehBoard
import me.kumatheta.feh.mcts.FehMove
import me.kumatheta.feh.readMap
import me.kumatheta.feh.readUnits
import me.kumatheta.mcts.Mcts
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
//    val playerMap = spawnMap.asSequence().map {
//        (it.key - 4) to it.value.heroModel
//    }.toMap()


    val heroModel = playerMap[1] ?: throw IllegalStateException()
    val state =
        BattleState(
            BasicBattleMap(
                positionMap,
                spawnMap,
                playerMap
            )
//                (1..4).associateWith { heroModel })
        )//mapOf(1 to Effie, 2 to Bartre, 3 to Fir)))
    val phraseLimit = 14
    val board = FehBoard(phraseLimit, state)
    val mcts = Mcts(board)

    val testMoves = listOf<UnitAction>(
        MoveOnly(2, Position(5, 6)),
        MoveOnly(3, Position(4, 4)),
        MoveOnly(1, Position(3, 5)),
        MoveOnly(4, Position(4, 5))
    ).map {
        FehMove(it)
    }
    val tryMoves = board.tryMoves(testMoves)
    println("${tryMoves.enemyDied}, ${tryMoves.playerDied}")
    repeat(10) {
        val duration = measureTime { mcts.run(1000) }
        println("duration $duration")
        val bestMoves = mcts.getBestMoves()
        println("best score: ${mcts.getBestScore()}")
        val testState = board.tryMoves(bestMoves)
        bestMoves.forEach {
            println(it)
        }
        println("${testState.enemyDied}, ${testState.playerDied}")
    }
}


