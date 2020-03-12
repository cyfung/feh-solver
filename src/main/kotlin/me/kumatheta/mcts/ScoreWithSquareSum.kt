package me.kumatheta.mcts

class ScoreWithSquareSum<T : Move>(
    override val totalScore: Long,
    override val tries: Int,
    override val bestScore: Long,
    override val moves: List<T>?,
    val scoreSquareSum: Long
) : Score<T>