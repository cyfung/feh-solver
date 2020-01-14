package me.kumatheta.mcts

class Score<T: Move>(
    val totalScore: Long,
    val tries: Int,
    val bestScore: Long,
    val moves: List<T>?,
    val scoreSquareSum: Long
)