package me.kumatheta.mcts

import java.util.concurrent.atomic.AtomicReference

class FakeNode<T : Move, S : Score<T>>(
    @Volatile override var parent: Node<T, S>?,
    override val lastMove: T?,
    override val scoreRef: AtomicReference<S>
) : Node<T, S> {
    override val childIndex: Int
        get() = throw UnsupportedOperationException()

    override val fakeNode: FakeNode<T, S>
        get() = throw UnsupportedOperationException()

    override suspend fun selectAndPlayOut(
        scoreManager: ScoreManager<T, S>,
        updateScore: (Long, List<T>) -> Unit
    ): Node<T, S>? {
        throw UnsupportedOperationException()
    }

    override fun removeChild(index: Int) {
        // because of race condition, this may still get called after FakeNode is replaced with the original node
    }

    override fun getBestChild(childSelector: (S) -> Long): Node<T, S>? {
        throw UnsupportedOperationException()
    }

    override fun noMoreChild(): Boolean {
        throw UnsupportedOperationException()
    }

    override fun onRemove() {
        throw UnsupportedOperationException()
    }

    override fun removeAllChildren() {
        throw UnsupportedOperationException()
    }
}