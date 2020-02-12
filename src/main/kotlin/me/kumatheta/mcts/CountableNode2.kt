package me.kumatheta.mcts

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.random.Random

class CountableNode2Manager<T : Move, S : Score<T>>(
    private val random: Random,
    private val scoreManager: ScoreManager<T, S>
) : NodeManager<T, S> {
    private val sizeRef = AtomicInteger(0)
    override val estimatedSize
        get() = sizeRef.get()

    fun getDelegateNode(countableNode2: CountableNode2<T, S>): Node<T, S> {
        while (true) {
            val delegate = countableNode2.delegate.get()
            if (delegate != null) {
                return delegate
            }
            val node = ThreadSafeNode(
                board = countableNode2.board,
                random = random,
                parent = countableNode2.parent,
                lastMove = countableNode2.lastMove,
                scoreRef = countableNode2.scoreRef,
                childIndex = countableNode2.childIndex,
                scoreManager = scoreManager,
                childBuilder = ::buildChild,
                isRoot = countableNode2.isRoot
            )
            if (countableNode2.delegate.compareAndSet(null, node)) {
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
        moves: List<T>
    ): Node<T, S> {
        sizeRef.incrementAndGet()
        return CountableNode2(
            countableNode2Manager = this,
            board = board,
            parent = parent,
            lastMove = lastMove,
            scoreRef = AtomicReference(scoreManager.newChildScore(childScore, moves)),
            childIndex = childIndex,
            isRoot = false
        )
    }



    fun invalidate(countableNode2: CountableNode2<T, S>) {
        sizeRef.decrementAndGet()
        val delegate = countableNode2.delegate.get() ?: return
        if (countableNode2.delegate.compareAndSet(delegate, null)) {
            delegate.onRemove()
        }
    }

    override fun createRootNode(board: Board<T>): Node<T, S> {
        sizeRef.incrementAndGet()
        return CountableNode2(
            countableNode2Manager = this,
            board = board,
            parent = null,
            lastMove = null,
            scoreRef = AtomicReference(scoreManager.newEmptyScore()),
            childIndex = 0,
            isRoot = true
        )
    }
}

class CountableNode2<T : Move, S : Score<T>>(
    private val countableNode2Manager: CountableNode2Manager<T, S>,
    val board: Board<T>,
    parent: Node<T, S>?,
    override val lastMove: T?,
    override val scoreRef: AtomicReference<S>,
    override val childIndex: Int,
    isRoot: Boolean
) : Node<T, S> {

    internal val delegate = AtomicReference<Node<T, S>?>(null)

    @Volatile
    override var parent = parent
        set(value) {
            field = value
            this.delegate.get()?.parent = value
        }

    @Volatile
    override var isRoot = isRoot
        set(value) {
            field = value
            this.delegate.get()?.isRoot = value
        }

    override fun getBestChild(): Node<T, S>? {
        return countableNode2Manager.getDelegateNode(this).getBestChild()
    }

    override suspend fun selectAndPlayOut(updateScore: (Long, List<T>) -> Unit): Node<T, S>? {
        return countableNode2Manager.getDelegateNode(this).selectAndPlayOut(updateScore)
    }

    override fun removeChild(index: Int) {
        this.delegate.get()?.removeChild(index)
    }

    override fun noMoreChild(): Boolean {
        return countableNode2Manager.getDelegateNode(this).noMoreChild()
    }

    override fun onRemove() {
        if (isRoot) {
            return
        }
        countableNode2Manager.invalidate(this)
    }
}