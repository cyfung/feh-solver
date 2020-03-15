package me.kumatheta.mcts

import java.util.concurrent.atomic.AtomicReference

interface Node<T : Move, S : Score<T>> {
    var parent: Node<T, S>?
    val lastMove: T?
    val scoreRef: AtomicReference<S>
    val childIndex: Int
    val fakeNode: FakeNode<T, S>

    suspend fun selectAndPlayOut(scoreManager: ScoreManager<T,S>, updateScore: (Long, List<T>) -> Unit): Node<T, S>?
    fun removeChild(index: Int)
    fun getBestChild(): Node<T, S>?
    fun noMoreChild(): Boolean

    fun onRemove()
    fun removeAllChildren()
}

val <T : Move, S : Score<T>> Node<T, S>.score: S
    get() = scoreRef.get()

val <T : Move, S : Score<T>> Node<T, S>.isFixed: Boolean
    get() = parent == null || parent is FakeNode

abstract class AbstractNode<T : Move, S : Score<T>>(
    parent: Node<T, S>?,
    lastMove: T?,
    scoreRef: AtomicReference<S>
) : Node<T, S> {
    override val fakeNode = FakeNode(parent, lastMove, scoreRef)

    override val lastMove: T?
        get() = fakeNode.lastMove
    override val scoreRef: AtomicReference<S>
        get() = fakeNode.scoreRef
    override var parent: Node<T, S>?
        get() {
            return fakeNode.parent
        }
        set(value) {
            fakeNode.parent = value
        }

}