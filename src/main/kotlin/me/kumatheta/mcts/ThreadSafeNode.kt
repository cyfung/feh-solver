package me.kumatheta.mcts

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.sqrt
import kotlin.random.Random

class ThreadSafeNode<T : Move> private constructor(
    board: Board<T>,
    private val explorationConstant: Double,
    private val random: Random,
    private val parent: ThreadSafeNode<T>?,
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

    private val moveQueue: ConcurrentLinkedQueue<T> = ConcurrentLinkedQueue(board.moves.shuffled(random))

    private val boardRef: AtomicReference<Board<T>?> = AtomicReference(
        if (moveQueue.isEmpty()) {
            null
        } else {
            board
        }
    )
    @Volatile
    override var playOutMove = movesAndScore?.first

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

    private val outstandingChildCount = AtomicInteger(moveQueue.size)

    private val children = ConcurrentLinkedQueue<ThreadSafeNode<T>>()

    private val isTerminalNode = moveQueue.isEmpty()

    private fun updateScore(newScore: Double) {
        scoreRef.getAndUpdate {
            Score(it.totalScore + newScore, max(it.bestScore, newScore), it.tries + 1)
        }
        parent?.updateScore(newScore)
    }

    override val bestScore
        get() = scoreRef.get().bestScore

    override val tries
        get() = scoreRef.get().tries

    override fun getBestChild(): ThreadSafeNode<T>? {
        val child = children.maxBy {
            it.bestScore
        }
        // return current play out instead of child's if it is better
        if (child == null || child.bestScore < bestScore) {
            return null
        }
        return child
    }

    override fun selectAndPlayOut(): ThreadSafeNode<T>? {
        check(!isTerminalNode)
        val board = boardRef.get()
        if (board == null) {
//            println("this is pruned")
//            updateScore(bestScore)
            return null
        }
        val move = moveQueue.poll()
        return if (move == null) {
            val tries = scoreRef.get().tries
            val noOutstanding = outstandingChildCount.get() == 0
            // select
            val child = children.asSequence().filterNot { it.isTerminalNode }.filterNot { it.boardRef.get() == null }.maxBy {
                val score = it.scoreRef.get()
                score.totalScore / score.tries + explorationConstant * sqrt(ln(tries.toDouble()) / score.tries.toDouble())
            }
            if (child == null) {
                // play out this node
//                updateScore(bestScore)

                if (noOutstanding) {
                    if (boardRef.getAndSet(null) != null) {
                        this.playOutMove = getBestMoves()
                        children.clear()
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
            val child = ThreadSafeNode(copy, explorationConstant, random, this, move, movesAndScore)
            updateScore(movesAndScore.second)
            children.add(child)
            outstandingChildCount.decrementAndGet()
            null
        }
    }
}
