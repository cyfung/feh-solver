package me.kumatheta.mcts

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.ln
import kotlin.math.sqrt
import kotlin.random.Random

class ThreadSafeNode<T : Move> private constructor(
    private val board: Board<T>,
    private val explorationConstant: Double,
    private val random: Random,
    private val parent: ThreadSafeNode<T>?,
    private val lastMove: T?,
    private val scoreRef: AtomicReference<Score<T>>
) : Node<T> {
    constructor(board: Board<T>, explorationConstant: Double, random: Random) : this(
        board,
        explorationConstant,
        random,
        null,
        null,
        AtomicReference(Score(0.0, 0, Double.MIN_VALUE, emptyList()))
    )

    private val moveQueue: ConcurrentLinkedQueue<T> = ConcurrentLinkedQueue(board.moves.shuffled(random))

    init {
        require(moveQueue.isNotEmpty())
    }

    private val outstandingChildCount = AtomicInteger(moveQueue.size)

    private val children = Collections.newSetFromMap(ConcurrentHashMap<ThreadSafeNode<T>, Boolean>())

    private fun updateScore(newScore: Double, moves: List<T>) {
        var currentNode = this
        val currentMoves = LinkedList<T>()
        currentMoves.addAll(moves)
        while (true) {
            val parent = currentNode.parent
            val movesCreator = if (parent == null){
                {
                    currentMoves.toList()
                }
            } else {
                { null }
            }
            currentNode.scoreRef.getAndUpdate { oldScore ->
                if (newScore > oldScore.bestScore) {
                    Score(
                        totalScore = oldScore.totalScore + newScore,
                        tries = oldScore.tries + 1,
                        bestScore = newScore,
                        moves = movesCreator()
                    )
                } else {
                    Score(
                        totalScore = oldScore.totalScore + newScore,
                        tries = oldScore.tries + 1,
                        bestScore = oldScore.bestScore,
                        moves = oldScore.moves
                    )
                }
            }
            if (parent == null) return
            val lastMove = currentNode.lastMove
            check(lastMove != null)
            currentMoves.addFirst(lastMove)
            currentNode = parent
        }
    }

    override val bestScore
        get() = scoreRef.get().bestScore

    override val tries
        get() = scoreRef.get().tries

    override val bestMoves: List<T>
        get() {
            check(lastMove == null)
            return scoreRef.get().moves ?: throw IllegalStateException()
        }

    override fun getBestChild(): ThreadSafeNode<T>? {
        val child = children.asSequence().maxBy {
            it.bestScore
        }
        // return current play out instead of child's if it is better
        if (child == null || child.bestScore < bestScore) {
            return null
        }
        return child
    }

    override fun selectAndPlayOut(): ThreadSafeNode<T>? {
        val move = moveQueue.poll()
        return if (move == null) {
            val tries = scoreRef.get().tries
            // select
            val child =
                children.asSequence().maxBy {
                    getSortingScore(it, tries)
                }
            if (child == null) {
                // play out this node
//                println("empty")
            }
            child
        } else {
            // expand
            val copy = board.copy()
            copy.applyMove(move)
            val score = copy.score
            if (score != null) {
                updateScore(score, listOf(move))
                onChildRemoved()
            } else {
                // play out
                val (childScore, moves) = copy.playOut(random)
                val child = ThreadSafeNode(
                    board = copy,
                    explorationConstant = explorationConstant,
                    random = random,
                    parent = this,
                    lastMove = move,
                    scoreRef = AtomicReference(Score(childScore, 1, childScore, null))
                )
                updateScore(childScore, (sequenceOf(move) + moves).toList())
                children.add(child)
            }
            null
        }
    }

    private fun getSortingScore(it: ThreadSafeNode<T>, tries: Int): Double {
        val score = it.scoreRef.get()
        return score.totalScore / score.tries + explorationConstant * sqrt(ln(tries.toDouble()) / score.tries.toDouble())
    }

    private fun onChildRemoved() {
        val count = outstandingChildCount.decrementAndGet()
        if (count == 0) {
            parent?.removeChild(this)
        }
    }

    private fun removeChild(child: ThreadSafeNode<T>) {
        val removed = children.remove(child)
        check(removed)
        onChildRemoved()
    }
}
