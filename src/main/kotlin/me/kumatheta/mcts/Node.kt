package me.kumatheta.mcts

import java.util.concurrent.atomic.AtomicReference

interface Node<T : Move> {
    val board: Board<T>
    val bestScore: Score<T>
    val parent: Node<T>?
    val lastMove: T?
    val scoreRef: AtomicReference<Score<T>>
    val childIndex: Int

    suspend fun selectAndPlayOut(updateScore: (Long, List<T>) -> Unit): Node<T>?
    fun removeChild(child: Node<T>)
}

