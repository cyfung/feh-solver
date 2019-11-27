package me.kumatheta.mcts

interface Node<T : Move> {
    val bestScore: Double
    val tries: Int

    fun getBestChild(): Node<T>?
    fun selectAndPlayOut(): Node<T>?
    val bestMoves: List<T>
}

