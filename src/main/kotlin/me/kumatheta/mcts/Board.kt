package me.kumatheta.mcts

interface Board<T : Move> {
    val moves: List<T>
    val score: Long?
    fun applyMove(move: T): Board<T>
    fun suggestedOrder(nextMoves: List<T>): Sequence<T>
}

interface Move