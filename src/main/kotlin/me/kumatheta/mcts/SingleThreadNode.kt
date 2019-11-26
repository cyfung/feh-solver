package me.kumatheta.mcts

import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.sqrt
import kotlin.random.Random

class SingleThreadNode<T : Move> private constructor(
    private val board: Board<T>,
    private val explorationConstant: Double,
    private val random: Random,
    private val parent: SingleThreadNode<T>?,
    override val lastMove: T?,
    movesAndScore: Pair<List<T>, Double>?
) : Node<T> {
    constructor(board: Board<T>, explorationConstant: Double, random: Random) : this(
        board,
        explorationConstant,
        random,
        null,
        null,
        null
    )

    override val playOutMove = movesAndScore?.first
    private var score: Score = if (movesAndScore == null) {
        Score(0.0, Double.MIN_VALUE, 0)
    } else {
        val score: Double = movesAndScore.second
        Score(score, score, 1)
    }

    private val moveQueue: LinkedList<T> = LinkedList(board.moves.shuffled(random))

    private val children = ConcurrentLinkedQueue<SingleThreadNode<T>>()

    private val isTerminalNode = moveQueue.isEmpty()

    private var isPruned = AtomicBoolean(false)

    private fun updateScore(newScore: Double) {
        score = Score(score.totalScore + newScore, max(score.bestScore, newScore), score.tries + 1)
        parent?.updateScore(newScore)
    }

    override val bestScore
        get() = score.bestScore

    override val tries
        get() = score.tries

    override fun getBestChild(): SingleThreadNode<T>? {
        val child = children.maxBy {
            it.bestScore
        }
        // return current play out instead of child's if it is better
        if (child == null || child.bestScore < bestScore) {
            return null
        }
        return child
    }

    override fun selectAndPlayOut(): SingleThreadNode<T>? {
        check(!isTerminalNode)
        if (isPruned.get()) {
            updateScore(bestScore)
            return null
        }
        val move = moveQueue.poll()
        return if (move == null) {
            val score = score
            val tries = score.tries
            // select
            val child = children.asSequence().filterNot { it.isTerminalNode }.filterNot { it.isPruned.get() }.maxBy {
                score.totalScore / score.tries + explorationConstant * sqrt(ln(tries.toDouble()) / score.tries.toDouble())
            }
            if (child == null) {
                // play out this node
                updateScore(bestScore)

                if (!isPruned.getAndSet(true)) {
                    val bestChild = getBestChild()
                    children.clear()
                    if (bestChild != null) {
                        children.add(bestChild)
                    }
                }
            }
            child
        } else {
            // expand
            val copy = board.copy()
            copy.applyMove(move)
            // play out
            val movesAndScore = copy.playOut(random)
            val child = SingleThreadNode(copy, explorationConstant, random, this, move, movesAndScore)
            updateScore(movesAndScore.second)
            children.add(child)
            null
        }
    }
}
