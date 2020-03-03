package me.kumatheta.mcts

import kotlin.math.ln
import kotlin.math.sqrt

class VaryingUCT<T : Move>(
    @Volatile var high: Long,
    @Volatile var average: Long,
    private val explorationConstantC: Double = sqrt(2.0)
) : ScoreManager<T, VaryingUCT.MyScore<T>> {
    class MyScore<T : Move>(
        override val totalScore: Long,
        override val tries: Int,
        override val bestScore: Long,
        override val moves: List<T>?
    ) : Score<T>

    override fun newChildScore(childScore: Long, moves: List<T>) =
        MyScore(childScore, 1, childScore, moves)

    override fun computeScore(childScore: MyScore<T>, tries: Int): Double {
        val childTries = childScore.tries
        val ref = average
//        val averageScore = (childScore.totalScore.toDouble() - ref) / (high - ref) / childTries // wrong but working
        val averageScore = (childScore.totalScore.toDouble() / childTries - ref) / (high - ref)
//        val averageScore = (childScore.bestScore.toDouble() - ref) / (high - ref) / 2
        return averageScore +
            explorationConstantC * sqrt(ln(tries.toDouble()) / childTries.toDouble())
    }

    override fun newEmptyScore(): MyScore<T> {
        return MyScore(0, 0, 0, null)
    }

    override fun updateScore(oldScore: MyScore<T>, newScore: Long, movesCreator: () -> List<T>?): MyScore<T> {
        val totalScore = oldScore.totalScore + newScore
        if(totalScore < 0) {
            throw IllegalStateException()
        }
        val tries = oldScore.tries + 1
        return if (newScore > oldScore.bestScore) {
            MyScore(
                totalScore = totalScore,
                tries = tries,
                bestScore = newScore,
                moves = movesCreator()
            )
        } else {
            MyScore(
                totalScore = totalScore,
                tries = tries,
                bestScore = oldScore.bestScore,
                moves = oldScore.moves
            )
        }
    }
}

