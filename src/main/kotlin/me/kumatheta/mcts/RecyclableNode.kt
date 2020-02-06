package me.kumatheta.mcts

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.RemovalCause
import java.util.concurrent.atomic.AtomicReference
import kotlin.random.Random

class RecycleManager<T : Move, S : Score<T>>(
    private val random: Random,
    val scoreManager: ScoreManager<T, S>,
    cacheCount: Long
) {
    private val cache =
        Caffeine.newBuilder().maximumSize(cacheCount).weakKeys().weakValues()
            .removalListener { key: RecyclableNode<T, S>?, value: Node<T, S>?, cause: RemovalCause ->
                key?.delegate = null
                if (key!=null && key.parent == null) {
                    println("key: $key removed cause: $cause")
                }
            }
            .build { recyclableNode: RecyclableNode<T, S> ->
                recyclableNode.createActualNode(random, scoreManager)
            }
//    private val removalCount = AtomicInteger(0)
//
//    val removed
//        get() = removalCount.get()

    val estimatedSize
        get() = cache.estimatedSize()

    fun cleanup() = cache.cleanUp()

    fun getDelegateNode(recyclableNode: RecyclableNode<T, S>): Node<T, S> {
        return cache.get(recyclableNode) ?: throw IllegalStateException()
    }

}

class RecyclableNode<T : Move, S : Score<T>>(
    private val recycleManager: RecycleManager<T, S>,
    private val board: Board<T>,
    parent: Node<T, S>?,
    override val lastMove: T?,
    scoreRef: AtomicReference<S>,
    override val childIndex: Int
) : Node<T, S> {
    @Volatile
    override var parent = parent
        set(value) {
            field = value
            recycleManager.getDelegateNode(this).parent = value
        }
    private val _scoreRef = scoreRef
    override val scoreRef: AtomicReference<S>
        get() = recycleManager.getDelegateNode(this).scoreRef

    override fun getBestChild(): Node<T, S>? {
        return recycleManager.getDelegateNode(this).getBestChild()
    }

    @Volatile
    internal var delegate: Node<T, S>? = null

    internal fun createActualNode(
        random: Random,
        scoreManager: ScoreManager<T, S>
    ): ThreadSafeNode<T, S> {
        val threadSafeNode = ThreadSafeNode(
            board = board,
            random = random,
            parent = parent,
            lastMove = lastMove,
            scoreRef = _scoreRef,
            childIndex = childIndex,
            scoreManager = scoreManager,
            childBuilder = ::buildChild
        )
        delegate = threadSafeNode
        return threadSafeNode
    }

    private fun buildChild(
        parent: Node<T, S>,
        childIndex: Int,
        board: Board<T>,
        lastMove: T,
        childScore: Long,
        moves: List<T>
    ): Node<T, S> {
        return RecyclableNode(
            recycleManager = recycleManager,
            board = board,
            parent = parent,
            lastMove = lastMove,
            scoreRef = AtomicReference(recycleManager.scoreManager.newChildScore(childScore, moves)),
            childIndex = childIndex
        )
    }

    override suspend fun selectAndPlayOut(updateScore: (Long, List<T>) -> Unit): Node<T, S>? {
        return recycleManager.getDelegateNode(this).selectAndPlayOut(updateScore)
    }

    override fun removeChild(child: Node<T, S>) {
        recycleManager.getDelegateNode(this).removeChild(child)
    }
}