package me.kumatheta.mcts

import kotlin.random.Random

interface ScoreManagerFactory<T : Move, S : Score<T>> {
    fun newScoreManager(): ScoreManager<T, S>
    fun newEmptyScore(): S
}

interface ScoreRefRequired {
    fun update(average: Long, high: Long)
}

class CombinedScoreManagerFactory<T : Move, S : Score<T>>(
    private val manager1: ScoreManager<T, S>,
    private val manager2: ScoreManager<T, S>,
    private val manager1Chance: Float
) : ScoreManagerFactory<T, S> {
    override fun newScoreManager(): ScoreManager<T, S> {
        return if (Random.nextFloat() <= manager1Chance) {
            manager1
        } else {
            manager2
        }
    }

    override fun newEmptyScore(): S {
        return manager1.newEmptyScore()
    }

}