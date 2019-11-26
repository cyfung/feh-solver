package me.kumatheta.mcts

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.sqrt
import kotlin.random.Random

class ThreadSafeNodeLowMemory<T : Move> private constructor(
    board: Board<T>,
    private val explorationConstant: Double,
    private val random: Random,
    private val parent: ThreadSafeNodeLowMemory<T>?,
    val lastMove: T?,
    movesAndScore: Pair<List<T>, Double>?
) {
    constructor(board: Board<T>, explorationConstant: Double, random: Random) : this(
        board,
        explorationConstant,
        random,
        null,
        null,
        null
    )

    val playOutMove = movesAndScore?.first
    private val scoreRef: AtomicReference<Score>

    init {
        scoreRef = AtomicReference(
            if (movesAndScore == null) {
                Score(0.0, Double.MIN_VALUE, 0)
            } else {
                val score: Double = movesAndScore.second
                Score(score, score, 1)
            }
        )
    }

    private val moveQueue: ConcurrentLinkedQueue<T> = ConcurrentLinkedQueue(board.moves.shuffled(random))
    private val outstandingChildCount = AtomicInteger(moveQueue.size)

    private val children = ConcurrentLinkedQueue<ThreadSafeNodeLowMemory<T>>()

    private val isTerminalNode = moveQueue.isEmpty()

    private var isPruned = AtomicBoolean(false)

    private fun updateScore(newScore: Double) {
        scoreRef.getAndUpdate {
            Score(it.totalScore + newScore, max(it.bestScore, newScore), it.tries + 1)
        }
        parent?.updateScore(newScore)
    }

    val bestScore
        get() = scoreRef.get().bestScore

    val tries
        get() = scoreRef.get().tries

    fun getBestChild(): ThreadSafeNodeLowMemory<T>? {
        val child = children.maxBy {
            it.bestScore
        }
        // return current play out instead of child's if it is better
        if (child == null || child.bestScore < bestScore) {
            return null
        }
        return child
    }

    fun selectAndPlayOut(board: Board<T>): ThreadSafeNodeLowMemory<T>? {
        check(!isTerminalNode)
        if (isPruned.get()) {
            println("this is pruned")
//            updateScore(bestScore)
            return null
        }
        if (lastMove != null) {
            board.applyMove(lastMove)
        }
        val move = moveQueue.poll()
        return if (move == null) {
            val tries = scoreRef.get().tries
            // select
            val child = children.asSequence().filterNot { it.isTerminalNode }.filterNot { it.isPruned.get() }.maxBy {
                val score = it.scoreRef.get()
                score.totalScore / score.tries + explorationConstant * sqrt(ln(tries.toDouble()) / score.tries.toDouble())
            }
            if (child == null) {
                // play out this node
//                updateScore(bestScore)

                if (outstandingChildCount.get() == 0) {
                    if (!isPruned.getAndSet(true)) {
                        val bestChild = getBestChild()
                        children.clear()
                        if (bestChild != null) {
                            children.add(bestChild)
                        }
                    }
                }
            }
            child
        } else {
            // expand
            board.applyMove(move)
            // play out
            val movesAndScore = board.playOut(random)
            val child = ThreadSafeNodeLowMemory(board, explorationConstant, random, this, move, movesAndScore)
            updateScore(movesAndScore.second)
            children.add(child)
            outstandingChildCount.decrementAndGet()
            null
        }
    }
}
