package me.kumatheta.mcts

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.ln
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.time.ExperimentalTime
import kotlin.time.MonoClock

class Mcts<T : Move>(
    board: Board<T>,
    private val explorationConstantC: Double,
    private val explorationConstantD: Double
) {
    private val recycleManager = RecycleManager(Random, ::getSortingScore)
    private val rootNode: Node<T> = RecyclableNode(
        recycleManager = recycleManager,
        board = board.copy(),
        parent = null,
        lastMove = null,
        scoreRef = AtomicReference(Score(0, 0, 0, null, 0)),
        childIndex = 0
    )
//    private val rootNode: Node<T> = ThreadSafeNode(
//        board = board.copy(),
//        random = Random,
//        parent = null,
//        lastMove = null,
//        scoreRef = AtomicReference(Score(0, 0, 0, null, 0)),
//        childIndex = 0,
//        childBuilder = ::buildChild,
//        computeScore = ::getSortingScore
//    )
//
//    private fun buildChild(parent: Node<T>, childIndex: Int, board: Board<T>, lastMove:T, childScore: Long, moves: List<T>): Node<T> {
//        return ThreadSafeNode(
//            board = board,
//            random = Random,
//            parent = parent,
//            lastMove = lastMove,
//            scoreRef = AtomicReference(Score(childScore, 1, childScore, moves, childScore * childScore)),
//            childIndex = childIndex,
//            childBuilder = ::buildChild,
//            computeScore = ::getSortingScore
//        )
//    }

    private fun getSortingScore(child: Node<T>, tries: Int): Double {
        val childScore = child.scoreRef.get()
        val childTries = childScore.tries
        val average = childScore.totalScore.toDouble() / childTries
        return average +
                explorationConstantC * sqrt(ln(tries.toDouble()) / childTries.toDouble()) +
                sqrt((childScore.scoreSquareSum - average * childScore.bestScore + explorationConstantD) / childTries)
    }


    private val dispatcher = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2) {
        val thread = Thread(it)
        thread.isDaemon = true
        thread
    }.asCoroutineDispatcher()

    @ExperimentalTime
    fun run(second: Int) {
        val clockMark = MonoClock.markNow()
        val count = AtomicInteger(0)
        runBlocking {
            supervisorScope {
                (1..20).map {
                    launch(dispatcher) {
                        while (clockMark.elapsedNow().inSeconds < second) {
                            repeat(10) {
                                selectAndPlayOut()
                            }
                            count.getAndAdd(10)
                        }
                    }
                }
            }
        }
        println("run count: ${count.get()}, estimatedSize: ${recycleManager.estimatedSize}")
    }

    val bestScore: Score<T>
        get() = rootNode.bestScore

    private suspend fun selectAndPlayOut() {
        var node = rootNode
        while (true) {
            val newNode = node.selectAndPlayOut { newScore, moves ->
                updateScore(node, newScore, moves)
            } ?: break
            node = newNode
        }
    }

    private fun updateScore(startingNode: Node<T>, newScore: Long, moves: List<T>) {
        var currentNode: Node<T> = startingNode
        val currentMoves = LinkedList<T>()
        currentMoves.addAll(moves)
        while (true) {
            val parent = currentNode.parent
            val movesCreator = if (parent == null) {
                {
                    currentMoves.toList()
                }
            } else {
                { null }
            }
            currentNode.scoreRef.getAndUpdate { oldScore ->
                val totalScore = oldScore.totalScore + newScore
                val scoreSquareSum = oldScore.scoreSquareSum + newScore * newScore
                if (scoreSquareSum < oldScore.scoreSquareSum) {
                    throw IllegalStateException()
                }
                val tries = oldScore.tries + 1
                if (newScore > oldScore.bestScore) {
                    Score(
                        totalScore = totalScore,
                        tries = tries,
                        bestScore = newScore,
                        moves = movesCreator(),
                        scoreSquareSum = scoreSquareSum
                    )
                } else {
                    Score(
                        totalScore = totalScore,
                        tries = tries,
                        bestScore = oldScore.bestScore,
                        moves = oldScore.moves,
                        scoreSquareSum = scoreSquareSum
                    )
                }
            }
            if (parent == null) return
            val lastMove = currentNode.lastMove
            check(lastMove != null)
            currentMoves.addFirst(lastMove)
            currentNode = parent
        }
    }


}