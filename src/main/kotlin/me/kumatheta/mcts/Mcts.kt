package me.kumatheta.mcts

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.sqrt
import kotlin.random.Random

class Mcts<T : Move>(board: Board<T>, explorationConstant: Double = sqrt(2.0)) {
    private val rootNode: Node<T> = ThreadSafeNode(board.copy(), explorationConstant, Random)
    private val dispatcher = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2) {
        val thread = Thread(it)
        thread.isDaemon = true
        thread
    }.asCoroutineDispatcher()

    fun run(times: Int) {
        val countDown = AtomicInteger(times)
        runBlocking {
            supervisorScope {
                (1..100).map {
                    launch(dispatcher) {
                        while (countDown.getAndAdd(-10) > 0) {
                            repeat(10) {
                                selectAndPlayOut()
                            }
                        }
                    }
                }
            }
        }
    }

    val bestScore: Double
        get() = rootNode.bestScore

    fun getBestMoves(): List<T> {
        return rootNode.getBestMoves()
    }

    val tries
        get() = rootNode.tries

    private fun selectAndPlayOut() {
        var node = rootNode
        while (true) {
            val newNode = node.selectAndPlayOut() ?: break
            node = newNode
        }
    }
}