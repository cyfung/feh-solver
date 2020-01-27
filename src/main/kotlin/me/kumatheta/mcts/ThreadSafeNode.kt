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

class ThreadSafeNode<T : Move>(
    override val board: Board<T>,
    private val random: Random,
    override val parent: Node<T>?,
    override val lastMove: T?,
    override val scoreRef: AtomicReference<Score<T>>,
    override val childIndex: Int,
    private val childBuilder: (parent: Node<T>, childScore: Long, moves: List<T>)->Node<T>,
    private val computeScore: (child: Node<T>, tries: Int)-> Double
) : Node<T> {

    private val children = ConcurrentHashMap<Int, Pair<T, CompletableDeferred<Node<T>?>>>()

    init {
        board.moves.asIterable().shuffled(random).asSequence().forEachIndexed { index, t ->
            children[index] = (t to CompletableDeferred())
        }
        require(children.isNotEmpty())
    }


    private val childInitTicket = AtomicInteger(children.size)

    private val outstandingChildCount = AtomicInteger(children.size)

    override val bestScore: Score<T>
        get() = scoreRef.get()

    override suspend fun selectAndPlayOut(updateScore: (Long, List<T>)->Unit): Node<T>? {
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
                        computeScore(it, tries)
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
                val child = childBuilder(this, childScore, moves)
                updateScore(childScore, (sequenceOf(move) + moves).toList())
                deferred.complete(child)
            }
            null
        }
    }

    private fun onChildRemoved() {
        val count = outstandingChildCount.decrementAndGet()
        if (count == 0) {
            parent?.removeChild(this)
        }
    }

    override fun removeChild(child: Node<T>) {
        children.remove(child.childIndex)
        onChildRemoved()
    }
}