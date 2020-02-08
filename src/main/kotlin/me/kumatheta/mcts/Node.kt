package me.kumatheta.mcts

import java.util.concurrent.atomic.AtomicReference

interface Node<T : Move, S : Score<T>> {
    val bestScore: S
        get() = scoreRef.get()
    var parent: Node<T, S>?
    val lastMove: T?
    val scoreRef: AtomicReference<S>
    val childIndex: Int

    suspend fun selectAndPlayOut(updateScore: (Long, List<T>) -> Unit): Node<T, S>?
    fun removeChild(index: Int)
    fun getBestChild(): Node<T, S>?
    fun noMoreChild(): Boolean

    fun onRemove()
}

