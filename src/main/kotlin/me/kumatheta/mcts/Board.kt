package me.kumatheta.mcts

interface Board<T : Move> {
    val moves: List<T>
    val score: Double?
    fun copy(): Board<T>
    fun applyMove(move: T)
}

interface Move