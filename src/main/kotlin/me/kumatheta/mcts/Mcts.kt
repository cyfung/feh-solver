package me.kumatheta.mcts

import kotlin.IllegalStateException
import kotlin.math.sqrt
import kotlin.random.Random

class Mcts<T : Move>(board: Board<T>, explorationConstant: Double = sqrt(2.0)) {
    private val rootNode = Node(board.copy(), explorationConstant, Random)

    fun run(times: Int) {
        repeat(times) {
            selectAndPlayOut()
        }
    }

    fun getBestScore(): Double {
        return rootNode.bestScore
    }

    fun getBestMoves(): List<T> {
        val bestRoute = generateSequence(rootNode.getBestChild()) {
            it.getBestChild()
        }.toList()

        return (bestRoute.asSequence().mapNotNull {
            it.lastMove ?: throw IllegalStateException()
        } + (bestRoute.last().playOutMove ?: throw IllegalStateException())).toList()
    }

    private fun selectAndPlayOut() {
        var node = rootNode
        while (true) {
            val newNode = node.selectAndPlayOut() ?: break
            node = newNode
        }
    }
}