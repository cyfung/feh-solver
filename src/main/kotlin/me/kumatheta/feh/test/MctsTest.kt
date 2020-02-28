package me.kumatheta.feh.test

import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import me.kumatheta.feh.BasicBattleMap
import me.kumatheta.feh.BattleState
import me.kumatheta.feh.MoveAndAssist
import me.kumatheta.feh.MoveAndAttack
import me.kumatheta.feh.MoveOnly
import me.kumatheta.feh.mcts.FehBoard
import me.kumatheta.feh.mcts.FehMove
import me.kumatheta.feh.message.UpdateInfo
import me.kumatheta.feh.readMap
import me.kumatheta.feh.readUnits
import me.kumatheta.mcts.Mcts
import me.kumatheta.mcts.RecyclableNode
import me.kumatheta.mcts.RecycleManager
import me.kumatheta.mcts.VaryingUCT
import me.kumatheta.ws.toUpdateInfoList
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.ExperimentalTime
import kotlin.time.MonoClock

@ExperimentalTime
fun main() {
    val dataSet = "grandmaster 51"
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
    val phraseLimit = 7
    var board = FehBoard(phraseLimit, state, 3)
    val testMoves = listOf(
        MoveAndAttack(heroUnitId = 4, moveTargetX = 3, moveTargetY = 3, attackTargetId = 10),
        MoveAndAttack(heroUnitId = 2, moveTargetX = 3, moveTargetY = 4, attackTargetId = 9),
        MoveAndAssist(heroUnitId = 3, moveTargetX = 4, moveTargetY = 4, assistTargetId = 2),
        MoveAndAttack(heroUnitId = 1, moveTargetX = 4, moveTargetY = 5, attackTargetId = 6),

        MoveAndAssist(heroUnitId = 2, moveTargetX = 4, moveTargetY = 3, assistTargetId = 1),
        MoveAndAttack(heroUnitId = 4, moveTargetX = 3, moveTargetY = 5, attackTargetId = 9),
        MoveAndAttack(heroUnitId = 3, moveTargetX = 4, moveTargetY = 4, attackTargetId = 7),
        MoveAndAttack(heroUnitId = 1, moveTargetX = 3, moveTargetY = 4, attackTargetId = 8),

        MoveAndAssist(heroUnitId = 2, moveTargetX = 3, moveTargetY = 3, assistTargetId = 4),
        MoveAndAttack(heroUnitId = 1, moveTargetX = 3, moveTargetY = 4, attackTargetId = 11),
            MoveAndAttack(heroUnitId = 3, moveTargetX = 5, moveTargetY = 3, attackTargetId = 17),
        MoveAndAttack(heroUnitId = 4, moveTargetX = 2, moveTargetY = 5, attackTargetId = 12)
    ).map {
        FehMove(it)
    }
    val testResult = board.tryMoves(testMoves, false)
    println("${testResult.enemyDied}, ${testResult.playerDied}, ${testResult.winningTeam}")

    println(Json.stringify(UpdateInfo.serializer().list, toUpdateInfoList(board, testMoves).second))
//
    testMoves.take(8).forEach { move ->
        val exists = board.moves.any {
            it == move
        }
        if (!exists) {
            throw IllegalStateException("moves not exists $move")
        }
        board = board.applyMove(move)
    }
    println(board.suggestedOrder(board.moves).map { it.unitAction }.toList())
    val scoreManager = VaryingUCT<FehMove>(3000, 2000, 1.0)
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
        println(
            "tries: ${score.tries - tries}, total tries: ${score.tries}, ${testState.enemyDied}, ${testState.playerDied}, ${testState.winningTeam}")
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


