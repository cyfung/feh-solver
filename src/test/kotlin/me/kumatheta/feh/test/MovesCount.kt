package me.kumatheta.feh.test

import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import me.kumatheta.feh.*
import me.kumatheta.feh.mcts.NormalMove
import me.kumatheta.feh.mcts.newFehBoard
import me.kumatheta.feh.mcts.toRating
import me.kumatheta.feh.mcts.toScore
import me.kumatheta.feh.mcts.tryAndGetDetails
import me.kumatheta.feh.mcts.tryMoves
import me.kumatheta.feh.message.UpdateInfo
import me.kumatheta.feh.util.CacheBattleMap
import me.kumatheta.ws.toUpdateInfoList
import java.nio.file.Paths
import kotlin.time.ExperimentalTime
import kotlin.time.MonoClock

val dumaMoveList: List<NormalMove>
    get() {
        return listOf(
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
    }

val sothisMoves: List<NormalMove>
    get() {
        return listOf(
            NormalMove(MoveOnly(heroUnitId = 2, moveTargetX = 3, moveTargetY = 3)),
            NormalMove(MoveOnly(heroUnitId = 4, moveTargetX = 3, moveTargetY = 0)),
            NormalMove(MoveOnly(heroUnitId = 1, moveTargetX = 3, moveTargetY = 1)),
            NormalMove(MoveOnly(heroUnitId = 3, moveTargetX = 2, moveTargetY = 0)),
            NormalMove(MoveAndAttack(heroUnitId = 2, moveTargetX = 2, moveTargetY = 2, attackTargetId = 13)),
            NormalMove(MoveAndAttack(heroUnitId = 1, moveTargetX = 4, moveTargetY = 1, attackTargetId = 13)),
            NormalMove(MoveAndAssist(heroUnitId = 4, moveTargetX = 3, moveTargetY = 1, assistTargetId = 1)),
            NormalMove(MoveAndAttack(heroUnitId = 3, moveTargetX = 3, moveTargetY = 0, attackTargetId = 13)),
            NormalMove(MoveAndAttack(heroUnitId = 1, moveTargetX = 3, moveTargetY = 2, attackTargetId = 9)),
            NormalMove(MoveAndAttack(heroUnitId = 3, moveTargetX = 3, moveTargetY = 0, attackTargetId = 12)),
            NormalMove(MoveAndAttack(heroUnitId = 1, moveTargetX = 3, moveTargetY = 2, attackTargetId = 12)),
            NormalMove(MoveAndAssist(heroUnitId = 4, moveTargetX = 3, moveTargetY = 1, assistTargetId = 1)),
            NormalMove(MoveAndAttack(heroUnitId = 1, moveTargetX = 3, moveTargetY = 4, attackTargetId = 15)),
            NormalMove(MoveOnly(heroUnitId = 2, moveTargetX = 3, moveTargetY = 2)),
            NormalMove(MoveAndAttack(heroUnitId = 3, moveTargetX = 3, moveTargetY = 3, attackTargetId = 5)),
            NormalMove(MoveOnly(heroUnitId = 2, moveTargetX = 4, moveTargetY = 3)),
            NormalMove(MoveAndAttack(heroUnitId = 1, moveTargetX = 3, moveTargetY = 4, attackTargetId = 11)),
            NormalMove(MoveAndAssist(heroUnitId = 4, moveTargetX = 3, moveTargetY = 2, assistTargetId = 3)),
            NormalMove(MoveOnly(heroUnitId = 3, moveTargetX = 2, moveTargetY = 5)),
            NormalMove(MoveOnly(heroUnitId = 1, moveTargetX = 3, moveTargetY = 3)),
            NormalMove(MoveAndAssist(heroUnitId = 4, moveTargetX = 2, moveTargetY = 3, assistTargetId = 1)),
            NormalMove(MoveAndAttack(heroUnitId = 1, moveTargetX = 3, moveTargetY = 3, attackTargetId = 18)),
            NormalMove(MoveAndAttack(heroUnitId = 3, moveTargetX = 1, moveTargetY = 6, attackTargetId = 16)),
            NormalMove(MoveAndAttack(heroUnitId = 2, moveTargetX = 3, moveTargetY = 2, attackTargetId = 18))
        )
    }

private val morganMorganMoves
    get() = listOf(
        NormalMove(MoveOnly(heroUnitId = 1, moveTargetX = 3, moveTargetY = 2)),
        NormalMove(MoveOnly(heroUnitId = 4, moveTargetX = 3, moveTargetY = 0)),
        NormalMove(MoveOnly(heroUnitId = 3, moveTargetX = 4, moveTargetY = 0)),
        NormalMove(MoveOnly(heroUnitId = 2, moveTargetX = 3, moveTargetY = 1)),
        NormalMove(MoveOnly(heroUnitId = 4, moveTargetX = 3, moveTargetY = 0)),
        NormalMove(MoveAndAttack(heroUnitId = 1, moveTargetX = 2, moveTargetY = 2, attackTargetId = 5)),
        NormalMove(MoveAndAttack(heroUnitId = 3, moveTargetX = 3, moveTargetY = 2, attackTargetId = 10)),
        NormalMove(MoveAndAttack(heroUnitId = 2, moveTargetX = 3, moveTargetY = 3, attackTargetId = 9)),
        NormalMove(MoveAndAttack(heroUnitId = 2, moveTargetX = 3, moveTargetY = 3, attackTargetId = 6)),
        NormalMove(MoveAndAttack(heroUnitId = 1, moveTargetX = 2, moveTargetY = 4, attackTargetId = 8)),
        NormalMove(MoveAndAttack(heroUnitId = 3, moveTargetX = 0, moveTargetY = 2, attackTargetId = 8)),
        NormalMove(MoveAndAssist(heroUnitId = 4, moveTargetX = 3, moveTargetY = 2, assistTargetId = 2)),
        NormalMove(MoveAndAttack(heroUnitId = 2, moveTargetX = 3, moveTargetY = 4, attackTargetId = 7))
    )

private val titaniaMistMoves = listOf(
//    Rearrange(listOf(1, 4, 3, 2)),
    NormalMove(MoveOnly(heroUnitId = 4, moveTargetX = 4, moveTargetY = 4)),
    NormalMove(MoveOnly(heroUnitId = 1, moveTargetX = 4, moveTargetY = 6)),
    NormalMove(MoveAndAssist(heroUnitId = 3, moveTargetX = 4, moveTargetY = 5, assistTargetId = 1)),
    NormalMove(MoveOnly(heroUnitId = 1, moveTargetX = 5, moveTargetY = 6)),
    NormalMove(MoveOnly(heroUnitId = 2, moveTargetX = 3, moveTargetY = 5)),
    NormalMove(MoveAndAttack(heroUnitId = 1, moveTargetX = 5, moveTargetY = 4, attackTargetId = 8)),
    NormalMove(MoveOnly(heroUnitId = 2, moveTargetX = 5, moveTargetY = 6)),
    NormalMove(MoveAndAssist(heroUnitId = 3, moveTargetX = 5, moveTargetY = 5, assistTargetId = 1)),
    NormalMove(MoveOnly(heroUnitId = 1, moveTargetX = 5, moveTargetY = 2)),
    NormalMove(MoveOnly(heroUnitId = 4, moveTargetX = 5, moveTargetY = 4)),
    NormalMove(MoveOnly(heroUnitId = 4, moveTargetX = 4, moveTargetY = 3)),
    NormalMove(MoveAndAttack(heroUnitId = 1, moveTargetX = 5, moveTargetY = 4, attackTargetId = 5)),
    NormalMove(MoveAndAssist(heroUnitId = 3, moveTargetX = 5, moveTargetY = 5, assistTargetId = 1)),
    NormalMove(MoveAndAttack(heroUnitId = 2, moveTargetX = 5, moveTargetY = 6, attackTargetId = 5)),
    NormalMove(MoveAndAttack(heroUnitId = 1, moveTargetX = 5, moveTargetY = 4, attackTargetId = 5)),
    NormalMove(MoveAndAttack(heroUnitId = 4, moveTargetX = 3, moveTargetY = 3, attackTargetId = 6)),
    NormalMove(MoveOnly(heroUnitId = 1, moveTargetX = 4, moveTargetY = 4)),
    NormalMove(MoveAndAssist(heroUnitId = 3, moveTargetX = 4, moveTargetY = 5, assistTargetId = 1)),
    NormalMove(MoveOnly(heroUnitId = 2, moveTargetX = 5, moveTargetY = 5)),
    NormalMove(MoveOnly(heroUnitId = 1, moveTargetX = 4, moveTargetY = 2)),
    NormalMove(MoveAndAttack(heroUnitId = 1, moveTargetX = 4, moveTargetY = 2, attackTargetId = 9))
)

val grandmaster51Moves = listOf(
    NormalMove(MoveAndAttack(heroUnitId = 4, moveTargetX = 3, moveTargetY = 3, attackTargetId = 10)),
    NormalMove(MoveAndAttack(heroUnitId = 2, moveTargetX = 3, moveTargetY = 4, attackTargetId = 9)),
    NormalMove(MoveAndAssist(heroUnitId = 3, moveTargetX = 4, moveTargetY = 4, assistTargetId = 2)),
    NormalMove(MoveAndAttack(heroUnitId = 1, moveTargetX = 4, moveTargetY = 5, attackTargetId = 6)),
    NormalMove(MoveAndAssist(heroUnitId = 2, moveTargetX = 4, moveTargetY = 3, assistTargetId = 1)),
    NormalMove(MoveAndAttack(heroUnitId = 3, moveTargetX = 4, moveTargetY = 4, attackTargetId = 7)),
    NormalMove(MoveAndAttack(heroUnitId = 4, moveTargetX = 3, moveTargetY = 5, attackTargetId = 9)),
    NormalMove(MoveAndAttack(heroUnitId = 1, moveTargetX = 3, moveTargetY = 4, attackTargetId = 8)),
    NormalMove(MoveAndAssist(heroUnitId = 2, moveTargetX = 3, moveTargetY = 3, assistTargetId = 4)),
    NormalMove(MoveAndAttack(heroUnitId = 1, moveTargetX = 3, moveTargetY = 4, attackTargetId = 11)),
    NormalMove(MoveAndAttack(heroUnitId = 4, moveTargetX = 2, moveTargetY = 5, attackTargetId = 12)),
    NormalMove(MoveAndAttack(heroUnitId = 3, moveTargetX = 5, moveTargetY = 3, attackTargetId = 17)),
    NormalMove(MoveAndAttack(heroUnitId = 4, moveTargetX = 1, moveTargetY = 5, attackTargetId = 14)),
    NormalMove(MoveOnly(heroUnitId = 2, moveTargetX = 3, moveTargetY = 2)),
    NormalMove(MoveAndAttack(heroUnitId = 1, moveTargetX = 3, moveTargetY = 5, attackTargetId = 15)),
    NormalMove(MoveAndAttack(heroUnitId = 3, moveTargetX = 3, moveTargetY = 3, attackTargetId = 18))
)

@ExperimentalCoroutinesApi
@ExperimentalTime
fun main() {
    val dataSet = "sothis infernal"
    Paths.get("data/$dataSet")
    val positionMap = readMap(Paths.get("data/$dataSet/$dataSet - map.csv"))
    val (_, spawnMap) = readUnits(Paths.get("data/$dataSet/$dataSet - spawn.csv"))
    val (playerMap, _) = readUnits(Paths.get("data/$dataSet/$dataSet - players.csv"))
    val battleMap = BasicBattleMap(
        positionMap,
        spawnMap,
        playerMap
    )
    val state = BattleState(CacheBattleMap(battleMap))
    state.rearrange((1..state.playerCount).toList())

    val testMoves = sothisMoves
    var board = newFehBoard(20, state, 3, false, toRating = UnitAction::toRating, calculateScore = BattleState::toScore)
    val test = board.tryMoves(testMoves)
    println(test.winningTeam)
    println(Json.stringify(UpdateInfo.serializer().list, toUpdateInfoList(board, testMoves).second))

    runBlocking {
        GlobalScope.launch {
            (10 downTo 0 step 2).forEach {
                countPhase(testMoves, state.copy(), it)
            }
        }.join()
    }
//    val testResult = board.tryMoves(testMoves, false)
//    println("${testResult.enemyDied}, ${testResult.playerDied}, ${testResult.winningTeam}")

//    println(Json.stringify(UpdateInfo.serializer().list, toUpdateInfoList(board, testMoves).second))
//
//    testMoves.take(0).forEach { move ->
//        val exists = board.moves.any {
//            it == move
//        }
//        if (!exists) {
//            throw IllegalStateException("moves not exists $move")
//        }
//        board = board.applyMove(move)
//    }
//    val scoreManager = LocalVaryingUCT<FehMove>(1.5)
//    val mcts = Mcts(board, scoreManager)
//    var tries = 0
//    val fixedMoves = mutableListOf<FehMove>()
//    val clockMark = MonoClock.markNow()
//    var lastFixMove = MonoClock.markNow()
//
//
//    repeat(10000) {
//        mcts.run(5)
//        val currentRootTries = mcts.rootScore.tries
//        if (currentRootTries > 1000000 || mcts.estimatedSize > 650000 || lastFixMove.elapsedNow().inMinutes > 20) {
//            mcts.moveDown()
//            lastFixMove = MonoClock.markNow()
//        }
//        println("current root tries $currentRootTries")
//        println("elapsed ${clockMark.elapsedNow()}")
//        val score = mcts.score
//        val bestMoves = score.moves ?: throw IllegalStateException()
//        val testState = try {
//            board.tryMoves(bestMoves)
//        } catch (t: Throwable) {
//            throw t
//        }
//        println("fixed:")
//        fixedMoves.forEach {
//            println(it)
//        }
//        println("changing:")
//        bestMoves.forEach {
//            println(it)
//        }
//        println("best score: ${score.bestScore}")
////        scoreManager.high = score.bestScore
////        scoreManager.average = score.totalScore / score.tries
//        println("totalScore = ${score.totalScore}")
//        println(
//            "tries: ${score.tries - tries}, total tries: ${score.tries}, ${testState.enemyDied}, ${testState.playerDied}, ${testState.winningTeam}"
//        )
//        tries = score.tries
//        println("estimatedSize: ${mcts.estimatedSize}")
//        println("elapsed ${clockMark.elapsedNow()}")
////        if (clockMark.elapsedNow().inSeconds > 100) {
////            Runtime.getRuntime().gc()
////            println("memory used ${(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000_000}")
////            return
////        }
//        println("memory used ${(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000_000}")
////        if (testState.enemyCount == testState.enemyDied && testState.playerDied == 0) {
////            return
////        }
//    }
}

@ExperimentalTime
private suspend fun countPhase(
    testMoves: List<NormalMove>,
    state: BattleState,
    phase: Int
) {
    testMoves.asSequence().map { it.unitAction }.forEach { action ->
        if (state.phase == phase) {
            println("actual result for phase ${state.phase} ${state.count()}")
            return
        }
        val allPlayerMovements = state.getAllPlayerMovements()
        println(allPlayerMovements.toList().size)
        val exists = allPlayerMovements.any {
            it == action
        }
        if (!exists) {
            throw IllegalStateException("moves not exists $action")
        }
        val movementResult = state.playerMove(action)
        if (state.playerDied > 0) {
            throw IllegalStateException()
        } else if (state.winningTeam != null) {
            println("winning team ${state.winningTeam}")
        } else {
            if (movementResult.phraseChange) {
                state.enemyMoves()
                if (state.playerDied > 0) {
                    throw IllegalStateException()
                } else if (state.winningTeam != null) {
                    println("winning team ${state.winningTeam}")
                }
            }
        }

    }
}

@ExperimentalTime
private var clockMark = MonoClock.markNow()

@ExperimentalTime
private suspend fun BattleState.count(): Pair<Int, Int> = coroutineScope {
    if (winningTeam == Team.PLAYER) {
        return@coroutineScope 1 to 1
    }
    val deferredList = getAllPlayerMovements().map {
        async {
            val copyState = copy()
            val movementResult = copyState.playerMove(it)
            if (copyState.playerDied > 0) {
                1 to 0
            } else {
                if (movementResult.phraseChange) {
                    copyState.enemyMoves()
                    if (copyState.playerDied > 0) {
                        1 to 0
                    } else {
                        1 to 1
                    }
                } else {
                    copyState.count()
                }
            }
        }
    }.toList()
    val pair = deferredList.map {
        it.await()
    }.reduce { pair1: Pair<Int, Int>, pair2: Pair<Int, Int> ->
        (pair1.first + pair2.first) to (pair1.second + pair2.second)
    }
    if (clockMark.elapsedNow().inSeconds > 10) {
        println("temp: $pair")
        clockMark = MonoClock.markNow()
    }
    pair
}


