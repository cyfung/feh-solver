package me.kumatheta.mcts

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import java.util.concurrent.Executors
import kotlin.math.sqrt
import kotlin.random.Random

class RootBasedMcts<T : Move>(board: Board<T>, explorationConstant: Double = 2 * sqrt(2.0), rootCount: Int) {
    private val rootNodes = (1..rootCount).map {
        SingleThreadNode(board.copy(), explorationConstant, Random)
    }
    private val dispatcher = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2) {
        val thread = Thread(it)
        thread.isDaemon = true
        thread
    }.asCoroutineDispatcher()

    fun run(times: Int) {
        runBlocking {
            supervisorScope {
                rootNodes.forEach { rootNode ->
                    launch(dispatcher) {
                        repeat(times) {
                            selectAndPlayOut(rootNode)
                        }
                    }
                }
            }
        }
    }

    val bestScore: Double
        get() = rootNodes.asSequence().map { it.bestScore }.max() ?: throw IllegalStateException()

    fun getBestMoves(): List<T> {
        val rootNode = rootNodes.maxBy { it.bestScore }?: throw IllegalStateException()
        val bestRoute = generateSequence(rootNode.getBestChild()) {
            it.getBestChild()
        }.toList()

        return (bestRoute.asSequence().mapNotNull {
            it.lastMove ?: throw IllegalStateException()
        } + (bestRoute.last().playOutMove ?: throw IllegalStateException())).toList()
    }

    val tries
        get() = rootNodes.asSequence().sumBy { it.tries }

    private fun selectAndPlayOut(rootNode: Node<T>) {
        var node = rootNode
        while (true) {
            val newNode = node.selectAndPlayOut() ?: break
            node = newNode
        }
    }
}