package me.kumatheta.mcts

interface Score<out T> {
    val totalScore: Long
    val tries: Int
    val bestScore: Long
    val moves: List<T>?
}

interface ScoreManager<T : Move, S : Score<T>> {
    fun newChildScore(childScore: Long, moves: List<T>): S
    fun computeScore(childScore: S, score: S): Double
    fun newEmptyScore(): S
    fun updateScore(oldScore: S, newScore: Long, movesCreator: () -> List<T>): S
}