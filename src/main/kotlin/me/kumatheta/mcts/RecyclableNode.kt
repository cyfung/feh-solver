package me.kumatheta.mcts

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.RemovalCause
import java.util.concurrent.atomic.AtomicReference
import kotlin.random.Random

class RecycleManager<T : Move>(
    private val random: Random,
    private val computeScore: (child: Node<T>, tries: Int) -> Double
) {
    private val cache =
        Caffeine.newBuilder().weakKeys().maximumSize(100_000)
            .removalListener { key: RecyclableNode<T>?, value: Node<T>?, cause: RemovalCause -> println("removed: $key $cause") }
            .build { recyclableNode: RecyclableNode<T> ->
                recyclableNode.createActualNode(random, computeScore)
            }

    val estimatedSize = cache.estimatedSize()

    fun getDelegateNode(recyclableNode: RecyclableNode<T>): Node<T> {
        return cache.get(recyclableNode) ?: throw IllegalStateException()
    }
}

class RecyclableNode<T : Move>(
    private val recycleManager: RecycleManager<T>,
    private val board: Board<T>,
    override val parent: Node<T>?,
    override val lastMove: T?,
    scoreRef: AtomicReference<Score<T>>,
    override val childIndex: Int
) : Node<T> {
    private val _scoreRef = scoreRef
    override val scoreRef: AtomicReference<Score<T>>
        get() = recycleManager.getDelegateNode(this).scoreRef

    internal fun createActualNode(
        random: Random,
        computeScore: (child: Node<T>, tries: Int) -> Double
    ): ThreadSafeNode<T> {
        return ThreadSafeNode(
            board = board,
            random = random,
            parent = parent,
            lastMove = lastMove,
            scoreRef = _scoreRef,
            childIndex = childIndex,
            computeScore = computeScore,
            childBuilder = ::buildChild
        )
    }

    private fun buildChild(
        parent: Node<T>,
        childIndex: Int,
        board: Board<T>,
        lastMove: T,
        childScore: Long,
        moves: List<T>
    ): Node<T> {
        return RecyclableNode(
            recycleManager = recycleManager,
            board = board,
            parent = parent,
            lastMove = lastMove,
            scoreRef = AtomicReference(Score(childScore, 1, childScore, moves, childScore * childScore)),
            childIndex = childIndex
        )
    }

    override suspend fun selectAndPlayOut(updateScore: (Long, List<T>) -> Unit): Node<T>? {
        return recycleManager.getDelegateNode(this).selectAndPlayOut(updateScore)
    }

    override fun removeChild(child: Node<T>) {
        recycleManager.getDelegateNode(this).removeChild(child)
    }
}