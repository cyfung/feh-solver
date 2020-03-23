package me.kumatheta.mcts

import javafx.scene.text.FontWeight

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

interface ScoreProvider {
    fun averageScore(
        parentAverage: Long,
        childAverage: Double,
        childBestScore: Long,
        normalizeFactor: Long
    ): Double
}

class WeightedScoreProvider(private val bestScoreWeight: Double = 0.15) : ScoreProvider {
    override fun averageScore(
        parentAverage: Long,
        childAverage: Double,
        childBestScore: Long,
        normalizeFactor: Long
    ): Double {
        val weighedAverage = (childAverage + bestScoreWeight * childBestScore) / (1 + bestScoreWeight)
        return (weighedAverage - parentAverage) / normalizeFactor
    }
}

object NormalScoreProvider : ScoreProvider {
    override fun averageScore(
        parentAverage: Long,
        childAverage: Double,
        childBestScore: Long,
        normalizeFactor: Long
    ): Double {
        return (childAverage - parentAverage) / normalizeFactor
    }
}