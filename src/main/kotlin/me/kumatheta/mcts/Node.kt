package me.kumatheta.mcts

interface Node<T : Move> {
    val bestScore: Score<T>

    suspend fun selectAndPlayOut(): Node<T>?
}

