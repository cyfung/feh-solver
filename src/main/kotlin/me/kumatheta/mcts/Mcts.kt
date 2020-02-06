package me.kumatheta.mcts

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.IllegalStateException
import kotlin.random.Random
import kotlin.time.ExperimentalTime
import kotlin.time.MonoClock

class Mcts<T : Move, S : Score<T>>(
    board: Board<T>,
    private val scoreManager: ScoreManager<T, S>,
    cacheCount: Long
) {
    private val recycleManager = RecycleManager(Random, scoreManager, cacheCount)
    @Volatile
    private var rootNode: Node<T, S> = RecyclableNode(
        recycleManager = recycleManager,
        board = board,
        parent = null,
        lastMove = null,
        scoreRef = AtomicReference(scoreManager.newEmptyScore()),
        childIndex = 0
    )

    private val dispatcher = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2) {
        val thread = Thread(it)
        thread.isDaemon = true
        thread
    }.asCoroutineDispatcher()

    val estimatedSize
        get() = recycleManager.estimatedSize

    fun cleanup() = recycleManager.cleanup()

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
        Runtime.getRuntime().gc()
        cleanup()
        println("run count: ${count.get()}")
    }

    val bestScore: S
        get() = rootNode.bestScore

    fun moveDown(): T {
        val bestChild = rootNode.getBestChild() ?: throw IllegalStateException("no more child")
        rootNode = bestChild
        bestChild.parent = null
        Runtime.getRuntime().gc()
        cleanup()
        return bestChild.lastMove ?: throw IllegalStateException("no move for child")
    }

    private suspend fun selectAndPlayOut() {
        var node = rootNode
        while (true) {
            val newNode = node.selectAndPlayOut { newScore, moves ->
                updateScore(node, newScore, moves)
            } ?: break
            node = newNode
        }
    }

    private fun updateScore(startingNode: Node<T, S>, newScore: Long, moves: List<T>) {
        var currentNode: Node<T, S> = startingNode
        val currentMoves = LinkedList<T>()
        currentMoves.addAll(moves)
        while (true) {
            val parent = currentNode.parent
            val movesCreator = {
                    currentMoves.toList()
                }
            currentNode.scoreRef.getAndUpdate { oldScore ->
                scoreManager.updateScore(oldScore, newScore, movesCreator)
            }
            if (parent == null) return
            val lastMove = currentNode.lastMove
            check(lastMove != null)
            currentMoves.addFirst(lastMove)
            currentNode = parent
        }
    }


}