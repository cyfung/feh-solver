package me.kumatheta.mcts

import kotlin.math.ln
import kotlin.math.sqrt

class VaryingUCT<T : Move>(
    @Volatile var high: Long,
    @Volatile var average: Long,
    private val explorationConstantC: Double = sqrt(2.0)
) : ScoreManager<T, UCTScore<T>> {

    override fun newChildScore(childScore: Long, moves: List<T>) =
        UCTScore(childScore, 1, childScore, moves)

    override fun computeScore(childScore: UCTScore<T>, score: UCTScore<T>): Double {
        val tries = score.tries
        val childTries = childScore.tries
        val ref = average
//        val averageScore = (childScore.totalScore.toDouble() - ref) / (high - ref) / childTries // wrong but working
        val averageScore = (childScore.totalScore.toDouble() / childTries - ref) / (high - ref)
//        val averageScore = (childScore.bestScore.toDouble() - ref) / (high - ref) / 2
        return averageScore +
                explorationConstantC * sqrt(ln(tries.toDouble()) / childTries.toDouble())
    }

    override fun newEmptyScore(): UCTScore<T> {
        return UCTScore(0, 0, -1, null)
    }

    override fun updateScore(oldScore: UCTScore<T>, newScore: Long, movesCreator: () -> List<T>?): UCTScore<T> {
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

