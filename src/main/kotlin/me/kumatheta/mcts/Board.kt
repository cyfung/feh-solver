package me.kumatheta.mcts

import me.kumatheta.feh.mcts.FehMove

interface Board<T : Move> {
    val moves: List<T>
    val score: Long?
    fun applyMove(move: T): Board<T>
    fun suggestedOrder(nextMoves: List<T>): Sequence<T>
}

interface Move