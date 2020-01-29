package me.kumatheta.mcts

import java.util.concurrent.atomic.AtomicReference

interface Node<T : Move, S : Score> {
    val bestScore: S
        get() = scoreRef.get()
    val parent: Node<T, S>?
    val lastMove: T?
    val scoreRef: AtomicReference<S>
    val childIndex: Int

    suspend fun selectAndPlayOut(updateScore: (Long, List<T>) -> Unit): Node<T, S>?
    fun removeChild(child: Node<T, S>)
    val bestChild: Node<T, S>?
}

