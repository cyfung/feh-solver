package me.kumatheta.mcts

import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.random.Random

@ExperimentalCoroutinesApi
class CountableNodeManager<T : Move, S : Score<T>>(
    private val random: Random
) : NodeManager<T, S> {
    private val sizeRef = AtomicInteger(0)
    override val estimatedSize
        get() = sizeRef.get()

    @ExperimentalCoroutinesApi
    fun getDelegateNode(countableNode: CountableNode<T, S>): Node<T, S> {
        while (true) {
            val delegate = countableNode.delegate.get()
            if (delegate != null) {
                return delegate
            }
            val node = ThreadSafeNode(
                board = countableNode.board,
                random = random,
                parent = countableNode.parent,
                lastMove = countableNode.lastMove,
                scoreRef = countableNode.scoreRef,
                childIndex = countableNode.childIndex,
                childBuilder = ::buildChild
            )
            if (countableNode.delegate.compareAndSet(null, node)) {
                return node
            } else {
                node.onRemove()
            }
        }
    }

    private fun buildChild(
        parent: Node<T, S>,
        childIndex: Int,
        board: Board<T>,
        lastMove: T,
        childScore: Long,
        moves: List<T>,
        scoreManager: ScoreManager<T, S>
    ): Node<T, S> {
        sizeRef.incrementAndGet()
        return CountableNode(
            countableNodeManager = this,
            board = board,
            parent = parent,
            lastMove = lastMove,
            scoreRef = AtomicReference(scoreManager.newChildScore(childScore, moves)),
            childIndex = childIndex,
            isFixed = false
        )
    }


    fun invalidate(countableNode: CountableNode<T, S>) {
        sizeRef.decrementAndGet()
        countableNode.removeAllChildren()
    }

    override fun createRootNode(board: Board<T>, emptyScore: S): Node<T, S> {
        sizeRef.incrementAndGet()
        return CountableNode(
            countableNodeManager = this,
            board = board,
            parent = null,
            lastMove = null,
            scoreRef = AtomicReference(emptyScore),
            childIndex = 0,
            isFixed = true
        )
    }
}

@ExperimentalCoroutinesApi
class CountableNode<T : Move, S : Score<T>>(
    private val countableNodeManager: CountableNodeManager<T, S>,
    val board: Board<T>,
    parent: Node<T, S>?,
    lastMove: T?,
    scoreRef: AtomicReference<S>,
    override val childIndex: Int,
    isFixed: Boolean
) : AbstractNode<T, S>(parent, lastMove, scoreRef) {
    internal val delegate = AtomicReference<Node<T, S>?>(null)

    override var parent: Node<T, S>?
        get() {
            return super.parent
        }
        set(value) {
            super.parent = value
            this.delegate.get()?.parent = value
        }

    @Volatile
    private var _isFixed = isFixed

    override fun getBestChild(childSelector: (S) -> Long): Node<T, S>? {
        return countableNodeManager.getDelegateNode(this).getBestChild(childSelector)
    }

    override suspend fun selectAndPlayOut(
        scoreManager: ScoreManager<T, S>,
        updateScore: (Long, List<T>) -> Unit
    ): Node<T, S>? {
        return countableNodeManager.getDelegateNode(this).selectAndPlayOut(scoreManager, updateScore)
    }

    override fun removeChild(index: Int) {
        this.delegate.get()?.removeChild(index)
    }

    override fun noMoreChild(): Boolean {
        return countableNodeManager.getDelegateNode(this).noMoreChild()
    }

    override fun onRemove() {
        if (isFixed) {
            return
        }
        countableNodeManager.invalidate(this)
    }

    override fun removeAllChildren() {
        val delegate = delegate.get() ?: return
        if (this.delegate.compareAndSet(delegate, null)) {
            delegate.onRemove()
        }
    }

    override suspend fun playOut(
        scoreManager: ScoreManager<T, S>,
        move: T,
        updateScore: (Long, List<T>) -> Unit
    ): Node<T, S>? {
        return countableNodeManager.getDelegateNode(this).playOut(scoreManager, move, updateScore)
    }

}