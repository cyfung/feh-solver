package me.kumatheta.mcts

class Score<T: Move>(
    val totalScore: Double,
    val tries: Int,
    val bestScore: Double,
    val moves: List<T>?
)