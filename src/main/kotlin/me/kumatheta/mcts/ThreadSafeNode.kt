package me.kumatheta.mcts

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.ln
import kotlin.math.sqrt
import kotlin.random.Random

@ExperimentalCoroutinesApi
class ThreadSafeNode<T : Move> private constructor(
    private val board: Board<T>,
    private val explorationConstantC: Double,
    private val explorationConstantD: Double,
    private val random: Random,
    private val parent: ThreadSafeNode<T>?,
    private val lastMove: T?,
    private val scoreRef: AtomicReference<Score<T>>,
    private val childIndex: Int
) : Node<T> {
    constructor(board: Board<T>, explorationConstantC: Double, explorationConstantD: Double, random: Random) : this(
        board,
        explorationConstantC,
        explorationConstantD,
        random,
        null,
        null,
        AtomicReference(Score(0, 0, 0, emptyList(), 0)),
        -1
    )

    private val children = ConcurrentHashMap<Int, Pair<T, CompletableDeferred<ThreadSafeNode<T>?>>>()

    init {
        board.moves.asIterable().shuffled().asSequence().forEachIndexed { index, t ->
            children[index] = (t to CompletableDeferred())
        }
        require(children.isNotEmpty())
    }


    private val childInitTicket = AtomicInteger(children.size)

    private val outstandingChildCount = AtomicInteger(children.size)

    private fun updateScore(newScore: Long, moves: List<T>) {
        var currentNode = this
        val currentMoves = LinkedList<T>()
        currentMoves.addAll(moves)
        while (true) {
            val parent = currentNode.parent
            val movesCreator = if (parent == null) {
                {
                    currentMoves.toList()
                }
            } else {
                { null }
            }
            currentNode.scoreRef.getAndUpdate { oldScore ->
                val totalScore = oldScore.totalScore + newScore
                val scoreSquareSum = oldScore.scoreSquareSum + newScore * newScore
                if (scoreSquareSum < oldScore.scoreSquareSum) {
                    throw IllegalStateException()
                }
                val tries = oldScore.tries + 1
                if (newScore > oldScore.bestScore) {
                    Score(
                        totalScore = totalScore,
                        tries = tries,
                        bestScore = newScore,
                        moves = movesCreator(),
                        scoreSquareSum = scoreSquareSum
                    )
                } else {
                    Score(
                        totalScore = totalScore,
                        tries = tries,
                        bestScore = oldScore.bestScore,
                        moves = oldScore.moves,
                        scoreSquareSum = scoreSquareSum
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

    override val bestScore: Score<T>
        get() = scoreRef.get()

    override suspend fun selectAndPlayOut(): ThreadSafeNode<T>? {
        val index = childInitTicket.decrementAndGet()
        return if (index < 0) {
            val tries = scoreRef.get().tries
            // select
            while (true) {
                val child =
                    children.values.asSequence().map { it.second }.mapNotNull {
                        if (it.isCompleted) {
                            it.getCompleted()
                        } else {
                            null
                        }
                    }.maxBy {
                        getSortingScore(it, tries)
                    }
                if (child != null) {
                    return child
                }
                val firstDeferred = children.values.firstOrNull() ?: break
                firstDeferred.second.await()
            }
            return null
        } else {
            val (move, deferred) = children[index] ?: throw IllegalStateException()
            // expand
            val copy = board.copy()
            copy.applyMove(move)
            val score = copy.score
            if (score != null) {
                updateScore(score, listOf(move))
                deferred.complete(null)
                children.remove(index)
                onChildRemoved()
            } else {
                // play out
                val (childScore, moves) = copy.playOut(random)
                val child = ThreadSafeNode(
                    board = copy,
                    explorationConstantC = explorationConstantC,
                    explorationConstantD = explorationConstantD,
                    random = random,
                    parent = this,
                    lastMove = move,
                    scoreRef = AtomicReference(Score(childScore, 1, childScore, null, childScore * childScore)),
                    childIndex = index
                )
                updateScore(childScore, (sequenceOf(move) + moves).toList())
                deferred.complete(child)
            }
            null
        }
    }

    private fun getSortingScore(child: ThreadSafeNode<T>, tries: Int): Double {
        val childScore = child.scoreRef.get()
        val childTries = childScore.tries
        val average = childScore.totalScore.toDouble() / childTries
        return average +
                explorationConstantC * sqrt(ln(tries.toDouble()) / childTries.toDouble()) +
                sqrt((childScore.scoreSquareSum - average * childScore.bestScore + explorationConstantD) / childTries)
    }

    private fun onChildRemoved() {
        val count = outstandingChildCount.decrementAndGet()
        if (count == 0) {
            parent?.removeChild(this)
        }
    }

    private fun removeChild(child: ThreadSafeNode<T>) {
        val removed = children.remove(child.childIndex)
        check(removed?.second?.getCompleted() == child)
        onChildRemoved()
    }
}
