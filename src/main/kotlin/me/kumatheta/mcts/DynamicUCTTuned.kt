package me.kumatheta.mcts

import kotlin.math.ln
import kotlin.math.sqrt

fun <T : Move> hybridDynamicUCTTune(): CombinedScoreManagerFactory<T, DynamicUCTTunedScore<T>> {
    return CombinedScoreManagerFactory<T, DynamicUCTTunedScore<T>>(
        manager1 = DynamicUCTTuned(),
        manager2 = DynamicUCTTuned(WeightedScoreProvider()),
        ratio = 1.0,
        childSelector = {
            (it.totalScore / it.tries + 0.15 * it.bestScore).toLong()
        }
    )
}

class DynamicUCTTunedScore<T : Move>(
    override val totalScore: Long,
    override val tries: Int,
    override val bestScore: Long,
    override val moves: List<T>?,
    val scoreSquareSum: Long
) : Score<T>

class DynamicUCTTuned<T : Move>(
    private val scoreProvider: ScoreProvider = NormalScoreProvider,
    private val explorationConstant: Double = 1.5
) : ScoreManager<T, DynamicUCTTunedScore<T>> {

    override fun newChildScore(childScore: Long, moves: List<T>) =
        DynamicUCTTunedScore(childScore, 1, childScore, moves, childScore * childScore)

    override fun computeScore(childScore: DynamicUCTTunedScore<T>, score: DynamicUCTTunedScore<T>): Double {
        val tries = score.tries
        val childTries = childScore.tries
        val average = score.totalScore / score.tries
        val high = score.bestScore
        val realAverage = childScore.totalScore.toDouble() / childTries
        val normalizeFactor = if (high <= average) {
            average
        } else {
            (high - average) * 2
        }
        val averageScore = scoreProvider.averageScore(average, realAverage, childScore.bestScore, normalizeFactor)
        val v =
            (childScore.scoreSquareSum.toDouble() / childTries - realAverage * realAverage) / normalizeFactor / normalizeFactor + sqrt(
                2 * ln(tries.toDouble()) / childTries.toDouble()
            )
        val exploration = minOf(0.25, v)
        return averageScore +
                explorationConstant * sqrt(exploration * ln(tries.toDouble()) / childTries.toDouble())
    }

    override fun newEmptyScore(): DynamicUCTTunedScore<T> {
        return DynamicUCTTunedScore(0, 0, Long.MIN_VALUE, null, 0)
    }

    override fun updateScore(
        oldScore: DynamicUCTTunedScore<T>,
        newScore: Long,
        movesCreator: () -> List<T>
    ): DynamicUCTTunedScore<T> {
        val totalScore = oldScore.totalScore + newScore
        if (totalScore < 0) {
            throw IllegalStateException()
        }
        val tries = oldScore.tries + 1
        val squareSum = oldScore.scoreSquareSum + newScore * newScore
        return if (newScore > oldScore.bestScore) {
            DynamicUCTTunedScore(
                totalScore = totalScore,
                tries = tries,
                bestScore = newScore,
                moves = movesCreator(),
                scoreSquareSum = squareSum
            )
        } else {
            DynamicUCTTunedScore(
                totalScore = totalScore,
                tries = tries,
                bestScore = oldScore.bestScore,
                moves = oldScore.moves,
                scoreSquareSum = squareSum
            )
        }
    }
}

