package me.kumatheta.mcts

import me.kumatheta.feh.mcts.FehMove

interface Board<T : Move> {
    val moves: List<T>
    val score: Long?
    fun copy(): Board<T>
    fun applyMove(move: T)
    fun suggestedMoves(nextMoves: List<T>): Sequence<T>
}

interface Move