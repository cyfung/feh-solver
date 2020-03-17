package me.kumatheta.mcts

import kotlin.math.ln
import kotlin.math.sqrt

class LocalVaryingUCTTuned<T : Move> : ScoreManager<T, ScoreWithSquareSum<T>> {

    override fun newChildScore(childScore: Long, moves: List<T>) =
        ScoreWithSquareSum(childScore, 1, childScore, moves, childScore * childScore)

    override fun computeScore(childScore: ScoreWithSquareSum<T>, score: ScoreWithSquareSum<T>): Double {
        val tries = score.tries
        val childTries = childScore.tries
        val average = score.totalScore / score.tries
        val high = score.bestScore
        val realAverage = childScore.totalScore.toDouble() / childTries
        val normalizeFactor = if(high <= average) {
            average
        } else {
            (high - average) * 2
        }
        val averageScore = (realAverage - average) / normalizeFactor
        val v =
            (childScore.scoreSquareSum.toDouble() / childTries - realAverage * realAverage) / normalizeFactor / normalizeFactor + sqrt(
                2 * ln(tries.toDouble()) / childTries.toDouble()
            )
        val exploration = minOf(0.25, v)
        return averageScore +
                1.5 * sqrt(exploration * ln(tries.toDouble()) / childTries.toDouble())
    }

    override fun newEmptyScore(): ScoreWithSquareSum<T> {
        return ScoreWithSquareSum(0, 0, -1, null, 0)
    }

    override fun updateScore(
        oldScore: ScoreWithSquareSum<T>,
        newScore: Long,
        movesCreator: () -> List<T>
    ): ScoreWithSquareSum<T> {
        val totalScore = oldScore.totalScore + newScore
        if (totalScore < 0) {
            throw IllegalStateException()
        }
        val tries = oldScore.tries + 1
        val squareSum = oldScore.scoreSquareSum + newScore * newScore
        return if (newScore > oldScore.bestScore) {
            ScoreWithSquareSum(
                totalScore = totalScore,
                tries = tries,
                bestScore = newScore,
                moves = movesCreator(),
                scoreSquareSum = squareSum
            )
        } else {
            ScoreWithSquareSum(
                totalScore = totalScore,
                tries = tries,
                bestScore = oldScore.bestScore,
                moves = oldScore.moves,
                scoreSquareSum = squareSum
            )
        }
    }
}

