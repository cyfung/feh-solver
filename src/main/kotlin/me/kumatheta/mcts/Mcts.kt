package me.kumatheta.mcts

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.sqrt
import kotlin.random.Random

class Mcts<T : Move>(board: Board<T>, explorationConstant: Double = 2 * sqrt(2.0)) {
    private val rootNode = Node(board.copy(), explorationConstant, Random)
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

    fun getBestScore(): Double {
        return rootNode.bestScore
    }

    fun getBestMoves(): List<T> {
        val bestRoute = generateSequence(rootNode.getBestChild()) {
            it.getBestChild()
        }.toList()

        return (bestRoute.asSequence().mapNotNull {
            it.lastMove ?: throw IllegalStateException()
        } + (bestRoute.last().playOutMove ?: throw IllegalStateException())).toList()
    }

    private fun selectAndPlayOut() {
        var node = rootNode
        while (true) {
            val newNode = node.selectAndPlayOut() ?: break
            node = newNode
        }
    }
}