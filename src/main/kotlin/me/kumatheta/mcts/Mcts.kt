package me.kumatheta.mcts

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.time.ExperimentalTime
import kotlin.time.MonoClock
import kotlin.time.measureTime

class Mcts<T : Move>(board: Board<T>, explorationConstant: Double = sqrt(2.0)) {
    private val rootNode: Node<T> = ThreadSafeNode(board.copy(), explorationConstant, Random)
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
        println("run count: ${count.get()}")
    }

    val bestScore: Double
        get() = rootNode.bestScore

    fun getBestMoves(): List<T> {
        return rootNode.bestMoves
    }

    val tries
        get() = rootNode.tries

    private suspend fun selectAndPlayOut() {
        var node = rootNode
        while (true) {
            val newNode = node.selectAndPlayOut() ?: break
            node = newNode
        }
    }
}