package me.kumatheta.mcts

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.random.Random

class CountableNodeManager<T : Move, S : Score<T>>(
    val random: Random,
    val scoreManager: ScoreManager<T, S>
) : NodeManager<T, S> {
    override fun createRootNode(board: Board<T>): Node<T, S> {
        nodeCount.incrementAndGet()
        return CountableNode(
            manager = this,
            board = board,
            parent = null,
            lastMove = null,
            scoreRef = AtomicReference(scoreManager.newEmptyScore()),
            childIndex = 0,
            isRoot = true
        )
    }

    val nodeCount = AtomicInteger(0)

    override val estimatedSize: Int
        get() = nodeCount.get()

    fun buildChild(
        parent: Node<T, S>,
        childIndex: Int,
        board: Board<T>,
        lastMove: T,
        childScore: Long,
        moves: List<T>
    ): CountableNode<T, S> {
        nodeCount.incrementAndGet()
        return CountableNode(
            manager = this,
            board = board,
            parent = parent,
            lastMove = lastMove,
            scoreRef = AtomicReference(scoreManager.newChildScore(childScore, moves)),
            childIndex = childIndex,
            isRoot = false
        )
    }
}

class CountableNode<T : Move, S : Score<T>> private constructor(
    private val manager: CountableNodeManager<T, S>,
    private val delegate: ThreadSafeNode<T, S>
) : Node<T, S> by delegate {

    constructor(
        manager: CountableNodeManager<T, S>,
        board: Board<T>,
        parent: Node<T, S>?,
        lastMove: T?,
        scoreRef: AtomicReference<S>,
        childIndex: Int,
        isRoot: Boolean
    ) : this(
        manager, ThreadSafeNode(
            board = board,
            random = manager.random,
            parent = parent,
            lastMove = lastMove,
            scoreRef = scoreRef,
            childIndex = childIndex,
            scoreManager = manager.scoreManager,
            childBuilder = manager::buildChild,
            isRoot = isRoot
        )
    )

    override fun onRemove() {
        if (isRoot) {
            return
        }
        delegate.onRemove()
        manager.nodeCount.decrementAndGet()
    }

}



