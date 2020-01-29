package me.kumatheta.mcts

interface Score {
    val tries: Int
    val bestScore: Long
}

interface ScoreManager<T: Move, S: Score> {
    fun newChildScore(childScore: Long, moves: List<T>): S
    fun computeScore(childScore: S, tries: Int): Double
    fun newEmptyScore(): S
    fun updateScore(oldScore: S, newScore: Long, movesCreator: () -> List<T>?): S
}