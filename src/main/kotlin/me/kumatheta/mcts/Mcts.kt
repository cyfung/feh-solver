package me.kumatheta.mcts

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.random.Random
import kotlin.time.ExperimentalTime
import kotlin.time.MonoClock

class Mcts<T : Move, S : Score<T>>(
    board: Board<T>,
    private val scoreManager: ScoreManager<T, S>
) {
    private val nodeManager = CountableNodeManager(Random, scoreManager)

    private val rootRef = AtomicReference(nodeManager.createRootNode(board))

    private val scoreRef = rootRef.get().scoreRef

    private val dispatcher = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2) {
        val thread = Thread(it)
        thread.isDaemon = true
        thread
    }.asCoroutineDispatcher()

    val estimatedSize
        get() = nodeManager.estimatedSize

    @ExperimentalTime
    fun run(second: Int) {
        val rootNode = rootRef.get()
        if (rootNode.noMoreChild()) {
            throw RuntimeException("no solution")
        }
        val clockMark = MonoClock.markNow()
        val count = AtomicInteger(0)
        runBlocking {
            supervisorScope {
                (1..2).map {
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
        println("run count: ${count.get()}")
    }

    val score: S
        get() = scoreRef.get()

    fun resetScore() : S {
        return scoreRef.getAndSet(scoreManager.newEmptyScore())
    }

    fun moveDown() {
        val newRoot = rootRef.updateAndGet { rootNode ->
            rootNode.getBestChild() ?: throw IllegalStateException("no more child")
        }
        val oldRoot = newRoot.parent ?: throw IllegalStateException()
        newRoot.parent = oldRoot.fakeNode
        oldRoot.removeAllChildren()
    }

    private suspend fun selectAndPlayOut() {
        var node: Node<T, S> = rootRef.get()
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
            if (parent == null) {
                return
            }
            val lastMove = currentNode.lastMove
            check(lastMove != null)
            currentMoves.addFirst(lastMove)
            currentNode = parent
        }
    }


}