package me.kumatheta.mcts

import kotlinx.coroutines.CompletableDeferred
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.random.Random

class ThreadSafeNode<T : Move, S : Score<T>>(
    private val board: Board<T>,
    private val random: Random,
    @Volatile
    override var parent: Node<T, S>?,
    override val lastMove: T?,
    override val scoreRef: AtomicReference<S>,
    override val childIndex: Int,
    private val childBuilder: (parent: Node<T, S>, childIndex: Int, board: Board<T>, lastMove: T, childScore: Long, moves: List<T>) -> Node<T, S>,
    private val scoreManager: ScoreManager<T, S>,
    @Volatile
    override var isRoot: Boolean
) : Node<T, S> {
    override fun noMoreChild() = outstandingChildCount.get() <= 0

    private val children = board.moves.asIterable().shuffled(random).asSequence().map {
        AtomicReference<Pair<T, CompletableDeferred<Node<T, S>?>>?>(it to CompletableDeferred())
    }.toList()

    private val childInitTicket = AtomicInteger(children.size)

    private val outstandingChildCount = AtomicInteger(children.size)

    override fun getBestChild(): Node<T, S>? {
        return children.asSequence().mapNotNull { it.get()?.second }.mapNotNull {
            if (it.isCompleted) {
                it.getCompleted()
            } else {
                null
            }
        }.maxBy {
            val score = it.scoreRef.get()
            score.totalScore.toDouble() / score.tries
        }
    }

    override suspend fun selectAndPlayOut(updateScore: (Long, List<T>) -> Unit): Node<T, S>? {
        val index = childInitTicket.decrementAndGet()
        return if (index < 0) {
            val tries = scoreRef.get().tries
            // select
            val child =
                children.asSequence().mapNotNull { it.get()?.second }.mapNotNull {
                    if (it.isCompleted) {
                        it.getCompleted()
                    } else {
                        null
                    }
                }.maxBy {
                    scoreManager.computeScore(it.scoreRef.get(), tries)
                }
            if (child != null) {
                return child
            }
            children.asSequence().mapNotNull {
                it.get()?.second
            }.forEach {
                val result = it.await()
                if (result != null) {
                    return result
                }
            }
//            val score = scoreRef.get()
//            updateScore(score.bestScore, score.moves?: throw IllegalStateException("no moves??"))
            return null
        } else {
            // the children could be removed because this is a newly generated node
            // and the child is removed from the call from an old child of the previous node
            val (move, deferred) = children[index].get() ?: return null
            // expand
            val copy = board.applyMove(move)
            val score = copy.score
            if (score != null) {
                updateScore(score, listOf(move))
                removeChild(index)
                require(deferred.isCompleted)
            } else {
                // play out
                val (childScore, moves) = copy.playOut(random)
                val child = childBuilder(this, index, copy, move, childScore, moves)
                updateScore(childScore, (sequenceOf(move) + moves).toList())
                val completed = deferred.complete(child)
                if (!completed) {
                    child.onRemove()
                }
            }
            null
        }
    }

    override fun removeChild(index: Int) {
        val ref = children[index]
        val child = ref.get() ?: return
        if (ref.compareAndSet(child, null)) {
            child.second.complete(null)
            val removedChild = child.second.getCompleted()
            val count = outstandingChildCount.decrementAndGet()
            if (count <= 0) {
                parent?.removeChild(this.childIndex)
            }
            removedChild?.onRemove()
        }
    }


    override fun onRemove() {
        if(isRoot) {
            return
        }
        children.asSequence().mapNotNull { it.get()?.second }.mapNotNull {
            it.complete(null)
            it.getCompleted()
        }.forEach {
            it.onRemove()
        }
    }
}