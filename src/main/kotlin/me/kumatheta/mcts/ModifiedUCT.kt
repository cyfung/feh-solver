package me.kumatheta.mcts

import kotlin.math.ln
import kotlin.math.sqrt

class ModifiedUCT<T : Move>(
    private val explorationConstantC: Double,
    private val explorationConstantD: Double
) : ScoreManager<T, ModifiedUCT.ScoreWithSquareSum<T>> {
    class ScoreWithSquareSum<T : Move>(
        override val totalScore: Long,
        override val tries: Int,
        override val bestScore: Long,
        override val moves: List<T>?,
        val scoreSquareSum: Long
    ) : Score<T>

    override fun newChildScore(childScore: Long, moves: List<T>) =
        ScoreWithSquareSum(childScore, 1, childScore, moves, childScore * childScore)

    override fun computeScore(childScore: ScoreWithSquareSum<T>, score: ScoreWithSquareSum<T>): Double {
        val tries = score.tries
        val childTries = childScore.tries
        val average = childScore.totalScore.toDouble() / childTries
        return average +
            explorationConstantC * sqrt(ln(tries.toDouble()) / childTries.toDouble()) +
            sqrt((childScore.scoreSquareSum - average * childScore.bestScore + explorationConstantD) / childTries)
    }

    override fun newEmptyScore(): ScoreWithSquareSum<T> {
        return ScoreWithSquareSum(0, 0, 0, null, 0)
    }

    override fun updateScore(oldScore: ScoreWithSquareSum<T>, newScore: Long, movesCreator: () -> List<T>?): ScoreWithSquareSum<T> {
        val totalScore = oldScore.totalScore + newScore
        val scoreSquareSum = oldScore.scoreSquareSum + newScore * newScore
        if (scoreSquareSum < oldScore.scoreSquareSum) {
            throw IllegalStateException()
        }
        val tries = oldScore.tries + 1
        return if (newScore > oldScore.bestScore) {
            ScoreWithSquareSum(
                totalScore = totalScore,
                tries = tries,
                bestScore = newScore,
                moves = movesCreator(),
                scoreSquareSum = scoreSquareSum
            )
        } else {
            ScoreWithSquareSum(
                totalScore = totalScore,
                tries = tries,
                bestScore = oldScore.bestScore,
                moves = oldScore.moves,
                scoreSquareSum = scoreSquareSum
            )
        }
    }
}

