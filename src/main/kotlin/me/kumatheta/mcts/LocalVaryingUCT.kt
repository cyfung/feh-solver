package me.kumatheta.mcts

import kotlin.math.ln
import kotlin.math.sqrt

fun <T : Move> hybridLocalVaryingUCT(): CombinedScoreManagerFactory<T, UCTScore<T>> {
    return CombinedScoreManagerFactory<T, UCTScore<T>>(
        manager1 = LocalVaryingUCT(explorationConstantC = 1.5),
            manager2 = LocalVaryingUCT(WeightedScoreProvider(0.1), explorationConstantC = 1.5),
        ratio = 6.0/4,
        childSelector = {
            (it.totalScore / it.tries + 0.15 * it.bestScore).toLong()
        }
    )
}

class LocalVaryingUCT<T : Move>(
    private val scoreProvider: ScoreProvider = NormalScoreProvider,
    private val explorationConstantC: Double = sqrt(2.0)
) : ScoreManager<T, UCTScore<T>> {

    override fun newChildScore(childScore: Long, moves: List<T>) =
        UCTScore(childScore, 1, childScore, moves)

    override fun computeScore(childScore: UCTScore<T>, score: UCTScore<T>): Double {
        val tries = score.tries
        val childTries = childScore.tries
        val ref = score.totalScore / score.tries
        val high = score.bestScore
        val normalizeFactor = if (high <= ref) {
            ref
        } else {
            high - ref
        }
        val realAverage = childScore.totalScore.toDouble() / childTries
        val averageScore = scoreProvider.averageScore(ref, realAverage, childScore.bestScore, normalizeFactor)
        return averageScore +
                explorationConstantC * sqrt(ln(tries.toDouble()) / childTries.toDouble())
    }

    override fun newEmptyScore(): UCTScore<T> {
        return UCTScore(0, 0, -1, null)
    }

    override fun updateScore(oldScore: UCTScore<T>, newScore: Long, movesCreator: () -> List<T>): UCTScore<T> {
        val totalScore = oldScore.totalScore + newScore
        if (totalScore < 0) {
            throw IllegalStateException()
        }
        val tries = oldScore.tries + 1
        return if (newScore > oldScore.bestScore) {
            UCTScore(
                totalScore = totalScore,
                tries = tries,
                bestScore = newScore,
                moves = movesCreator()
            )
        } else {
            UCTScore(
                totalScore = totalScore,
                tries = tries,
                bestScore = oldScore.bestScore,
                moves = oldScore.moves
            )
        }
    }
}

