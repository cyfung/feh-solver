package me.kumatheta.mcts

import me.kumatheta.feh.mcts.FehMove
import kotlin.math.ln
import kotlin.math.sqrt

fun hybridDynamicUCTTune(): CombinedScoreManagerFactory<FehMove, DynamicUCTTunedScore<FehMove>> {
    return CombinedScoreManagerFactory(
        manager1 = DynamicUCTTuned<FehMove>(bestScoreWeight = 0.0),
        manager2 = DynamicUCTTuned<FehMove>(),
        manager1Chance = 0.7f
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
    private val bestScoreWeight: Double = 0.15,
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
        val weighedAverage = (realAverage + bestScoreWeight * childScore.bestScore) / (1 + bestScoreWeight)
        val normalizeFactor = (high - average) * 2
        val averageScore = (weighedAverage - average) / normalizeFactor
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
        movesCreator: () -> List<T>?
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

