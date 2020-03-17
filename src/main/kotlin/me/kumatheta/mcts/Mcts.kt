package me.kumatheta.mcts

import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.random.Random
import kotlin.time.ExperimentalTime
import kotlin.time.MonoClock

@ExperimentalCoroutinesApi
class Mcts<T : Move, out S : Score<T>>(
    board: Board<T>,
    private val scoreManagerFactory: ScoreManagerFactory<T, S>
) {
    private val nodeManager = CountableNodeManager<T, S>(Random)

    private val rootRef = AtomicReference(nodeManager.createRootNode(board, scoreManagerFactory.newEmptyScore()))

    private val scoreRef = rootRef.get().scoreRef

    private val recentScoreRef = AtomicReference(scoreManagerFactory.newEmptyScore())

    private val dispatcher = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2) {
        val thread = Thread(it)
        thread.isDaemon = true
        thread
    }.asCoroutineDispatcher()

    val estimatedSize
        get() = nodeManager.estimatedSize

    @ExperimentalTime
    fun run(second: Int, parallelCount: Int = 20) {
        val rootNode = rootRef.get()
        if (rootNode.noMoreChild()) {
            throw RuntimeException("no solution")
        }
        val clockMark = MonoClock.markNow()
        val count = AtomicInteger(0)
        runBlocking {
            supervisorScope {
                repeat(parallelCount) {
                    launch(dispatcher) {
                        while (clockMark.elapsedNow().inSeconds < second) {
                            repeat(10) {
                                selectAndPlayOut(scoreManagerFactory.newScoreManager())
                            }
                            count.getAndAdd(10)
                        }
                    }
                }
            }
        }
        println("run count: ${count.get()}")
    }

    val score: S
        get() = scoreRef.get()

    val rootScore: S
        get() = rootRef.get().score

    fun resetRecentScore(): S {
        return recentScoreRef.getAndSet(scoreManagerFactory.newEmptyScore())
    }

    private val moveDownCountRef = AtomicInteger(0)
    val moveDownCount
        get() = moveDownCountRef.get()

    fun moveDown() {
        val newRoot = rootRef.updateAndGet { rootNode ->
            rootNode.getBestChild() ?: throw IllegalStateException("no more child")
        }
        val oldRoot = newRoot.parent ?: throw IllegalStateException()
        newRoot.parent = oldRoot.fakeNode
        oldRoot.removeAllChildren()
        moveDownCountRef.getAndIncrement()
    }

    private suspend fun selectAndPlayOut(scoreManager: ScoreManager<T, S>) {
        var node: Node<T, S> = rootRef.get()
        while (true) {
            val newNode = node.selectAndPlayOut(scoreManager) { newScore, moves ->
                updateScore(scoreManager, node, newScore, moves)
            } ?: break
            node = newNode
        }
    }

    private fun updateScore(
        scoreManager: ScoreManager<T, S>,
        startingNode: Node<T, S>,
        newScore: Long,
        moves: List<T>
    ) {
        var currentNode: Node<T, S> = startingNode
        val currentMoves = LinkedList<T>()
        currentMoves.addAll(moves)
        val movesCreator = {
            currentMoves.toList()
        }
        while (true) {
            val parent = currentNode.parent
            currentNode.scoreRef.getAndUpdate { oldScore ->
                scoreManager.updateScore(oldScore, newScore, movesCreator)
            }
            if (parent == null) {
                recentScoreRef.getAndUpdate { oldScore ->
                    scoreManager.updateScore(oldScore, newScore, movesCreator)
                }
                return
            }
            val lastMove = currentNode.lastMove
            check(lastMove != null)
            currentMoves.addFirst(lastMove)
            currentNode = parent
        }
    }


}