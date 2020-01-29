package me.kumatheta.mcts

import com.github.benmanes.caffeine.cache.Caffeine
import java.util.concurrent.atomic.AtomicReference
import kotlin.random.Random

class RecycleManager<T : Move, S : Score>(
    private val random: Random,
    val scoreManager: ScoreManager<T, S>,
    cacheCount: Long
) {
    private val cache =
        Caffeine.newBuilder().weakKeys().maximumSize(cacheCount)
//            .removalListener { key: RecyclableNode<T>?, value: Node<T>?, cause: RemovalCause -> removalCount.incrementAndGet() }
            .build { recyclableNode: RecyclableNode<T, S> ->
                recyclableNode.createActualNode(random, scoreManager)
            }
//    private val removalCount = AtomicInteger(0)
//
//    val removed
//        get() = removalCount.get()

    val estimatedSize
        get() = cache.estimatedSize()

    fun getDelegateNode(recyclableNode: RecyclableNode<T, S>): Node<T,S> {
        return cache.get(recyclableNode) ?: throw IllegalStateException()
    }

}

class RecyclableNode<T : Move, S : Score>(
    private val recycleManager: RecycleManager<T, S>,
    private val board: Board<T>,
    override val parent: Node<T, S>?,
    override val lastMove: T?,
    scoreRef: AtomicReference<S>,
    override val childIndex: Int
) : Node<T,S> {
    private val _scoreRef = scoreRef
    override val scoreRef: AtomicReference<S>
        get() = recycleManager.getDelegateNode(this).scoreRef
    override val bestChild
        get() = recycleManager.getDelegateNode(this).bestChild

    internal fun createActualNode(
        random: Random,
        scoreManager: ScoreManager<T,S>
    ): ThreadSafeNode<T,S> {
        return ThreadSafeNode(
            board = board,
            random = random,
            parent = parent,
            lastMove = lastMove,
            scoreRef = _scoreRef,
            childIndex = childIndex,
            scoreManager = scoreManager,
            childBuilder = ::buildChild
        )
    }

    private fun buildChild(
        parent: Node<T,S>,
        childIndex: Int,
        board: Board<T>,
        lastMove: T,
        childScore: Long,
        moves: List<T>
    ): Node<T,S> {
        return RecyclableNode(
            recycleManager = recycleManager,
            board = board,
            parent = parent,
            lastMove = lastMove,
            scoreRef = AtomicReference(recycleManager.scoreManager.newChildScore(childScore, moves)),
            childIndex = childIndex
        )
    }

    override suspend fun selectAndPlayOut(updateScore: (Long, List<T>) -> Unit): Node<T,S>? {
        return recycleManager.getDelegateNode(this).selectAndPlayOut(updateScore)
    }

    override fun removeChild(child: Node<T,S>) {
        recycleManager.getDelegateNode(this).removeChild(child)
    }
}