package me.kumatheta.mcts

import kotlin.math.ln
import kotlin.math.sqrt
import kotlin.random.Random

class Node<T : Move>(
    private val board: Board<T>,
    private val parent: Node<T>?,
    private val random: Random,
    val lastMove: T?,
    movesAndScore: Pair<List<T>, Double>? = null
) {
    val playOutMove = movesAndScore?.first
    var score: Double = movesAndScore?.second ?: 0.0
        private set
    var tries: Int = if (movesAndScore == null) 0 else 1
        private set

    private val moveIterator = board.moves.shuffled(random).iterator()
    private val children = mutableListOf<Node<T>>()

    private val isTerminalNode = !moveIterator.hasNext()

    private fun updateScore(newScore: Double) {
        score += newScore
        tries++
        parent?.updateScore(newScore)
    }

    fun getBestChild(): Node<T>? {
        return children.maxBy {
            it.score / it.tries
        }
    }

    fun selectAndPlayOut(): Node<T>? {
        if (isTerminalNode) {
            updateScore(board.score ?: throw IllegalStateException())
        }
        return if (moveIterator.hasNext()) {
            val move = moveIterator.next()
            val copy = board.copy()
            copy.applyMove(move)
            val movesAndScore = copy.playOut(random)
            val child = Node(copy, this, random, move, movesAndScore)
            children.add(child)
            null
        } else {
            val child = children.maxBy {
                it.score / it.tries + 2 * sqrt(ln(tries.toDouble()) / tries)
            }
            child
        }
    }
}

private fun <T : Move> Board<T>.playOut(random: Random): Pair<List<T>, Double> {
    var score = this.score
    if (score != null) {
        return Pair(emptyList(), score)
    }
    val test = copy()
    val moves = generateSequence {
        score = test.score
        if (score != null) {
            return@generateSequence null
        }
        val move = test.moves.random(random = random)
        test.applyMove(move)
        move
    }.toList()
    return Pair(moves, score ?: throw IllegalStateException())
}

