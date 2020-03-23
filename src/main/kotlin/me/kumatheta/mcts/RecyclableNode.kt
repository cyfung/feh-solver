package me.kumatheta.mcts

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.RemovalCause
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.concurrent.atomic.AtomicReference
import kotlin.random.Random

@ExperimentalCoroutinesApi
class RecycleManager<T : Move, S : Score<T>>(
    private val random: Random,
    cacheCount: Long
) {
    private val cache =
        Caffeine.newBuilder().maximumSize(cacheCount)
            .removalListener { _: RecyclableNode<T, S>?, value: Node<T, S>?, _: RemovalCause ->
                value?.onRemove()
            }
            .build { recyclableNode: RecyclableNode<T, S> ->
                recyclableNode.createActualNode(random)
            }

    val estimatedSize
        get() = cache.estimatedSize()

    fun cleanup() = cache.cleanUp()

    fun getDelegateNode(recyclableNode: RecyclableNode<T, S>): Node<T, S> {
        return cache.get(recyclableNode) ?: throw IllegalStateException()
    }

    fun getDelegateNodeIfPresent(recyclableNode: RecyclableNode<T, S>): Node<T, S>? {
        return cache.getIfPresent(recyclableNode)
    }

    fun invalidate(recyclableNode: RecyclableNode<T, S>) {
        cache.invalidate(recyclableNode)
    }
}

@ExperimentalCoroutinesApi
class RecyclableNode<T : Move, S : Score<T>>(
    private val recycleManager: RecycleManager<T, S>,
    private val board: Board<T>,
    parent: Node<T, S>?,
    lastMove: T?,
    scoreRef: AtomicReference<S>,
    override val childIndex: Int
) : AbstractNode<T, S>(parent, lastMove, scoreRef) {

    override var parent
        get() = super.parent
        set(value) {
            super.parent = parent
            recycleManager.getDelegateNode(this).parent = value
        }

    override fun getBestChild(childSelector: (S) -> Long): Node<T, S>? {
        return recycleManager.getDelegateNode(this).getBestChild(childSelector)
    }

    @ExperimentalCoroutinesApi
    internal fun createActualNode(
        random: Random
    ): ThreadSafeNode<T, S> {
        return ThreadSafeNode(
            board = board,
            random = random,
            parent = parent,
            lastMove = lastMove,
            scoreRef = scoreRef,
            childIndex = childIndex,
            childBuilder = ::buildChild
        )
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
        return RecyclableNode(
            recycleManager = recycleManager,
            board = board,
            parent = parent,
            lastMove = lastMove,
            scoreRef = AtomicReference(scoreManager.newChildScore(childScore, moves)),
            childIndex = childIndex
        )
    }

    override suspend fun selectAndPlayOut(
        scoreManager: ScoreManager<T, S>,
        updateScore: (Long, List<T>) -> Unit
    ): Node<T, S>? {
        return recycleManager.getDelegateNode(this).selectAndPlayOut(scoreManager, updateScore)
    }

    override fun removeChild(index: Int) {
        recycleManager.getDelegateNodeIfPresent(this)?.removeChild(index)
    }

    override fun noMoreChild(): Boolean {
        return recycleManager.getDelegateNode(this).noMoreChild()
    }

    override fun onRemove() {
        if (isFixed) {
            return
        }
        recycleManager.invalidate(this)
    }

    override fun removeAllChildren() {
        TODO("Not yet implemented")
    }
}