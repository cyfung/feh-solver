package me.kumatheta.mcts

import kotlin.random.Random

interface ScoreManagerFactory<T : Move, S : Score<T>> {
    fun newScoreManager(): ScoreManager<T, S>
    fun newEmptyScore(): S
}

interface ScoreRefRequired {
    fun updateScoreRef(average: Long, high: Long)
}

class CombinedScoreManagerFactory<T : Move, S : Score<T>>(
    private val manager1: ScoreManager<T, S>,
    private val manager2: ScoreManager<T, S>,
    ratio: Double
) : ScoreManagerFactory<T, S> {
    private val manager1Chance = ratio / (1 + ratio)

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

fun <T : Move, S : Score<T>> ScoreManager<T, S>.toFactory(): ScoreManagerFactory<T, S> {
    return if (this is ScoreRefRequired) {
        object : ScoreManagerFactory<T, S>, ScoreRefRequired {
            override fun newScoreManager(): ScoreManager<T, S> {
                return this@toFactory
            }

            override fun newEmptyScore(): S {
                return this@toFactory.newEmptyScore()
            }

            override fun updateScoreRef(average: Long, high: Long) {
                this@toFactory.updateScoreRef(average, high)
            }
        }
    } else {
        object : ScoreManagerFactory<T, S> {
            override fun newScoreManager(): ScoreManager<T, S> {
                return this@toFactory
            }

            override fun newEmptyScore(): S {
                return this@toFactory.newEmptyScore()
            }
        }
    }
}