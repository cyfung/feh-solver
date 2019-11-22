package me.kumatheta.mcts

import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.sqrt
import kotlin.random.Random

class Node<T : Move> private constructor(
    private val board: Board<T>,
    private val explorationConstant: Double,
    private val random: Random,
    private val parent: Node<T>?,
    val lastMove: T?,
    movesAndScore: Pair<List<T>, Double>?
) {
    constructor(board: Board<T>, explorationConstant: Double, random: Random) : this(
        board,
        explorationConstant,
        random,
        null,
        null,
        null
    )

    val playOutMove = movesAndScore?.first
    var score: Double = movesAndScore?.second ?: 0.0
        private set
    var bestScore = score
        private set
    var tries: Int = if (movesAndScore == null) 0 else 1
        private set

    private val moveQueue: ConcurrentLinkedQueue<T> = ConcurrentLinkedQueue(board.moves.shuffled(random))

    private val children = ConcurrentLinkedQueue<Node<T>>()

    private val isTerminalNode = moveQueue.isEmpty()
    private var isPruned = false

    private fun updateScore(newScore: Double) {
        bestScore = max(bestScore, newScore)
        score += newScore
        tries++
        parent?.updateScore(newScore)
    }

    fun getBestChild(): Node<T>? {
        val child = children.maxBy {
            it.bestScore
        }
        // return current play out instead of child's if it is better
        if (child == null || child.bestScore < bestScore) {
            return null
        }
        return child
    }

    fun selectAndPlayOut(): Node<T>? {
        check(!isTerminalNode)
        if (isPruned) {
            check(parent == null)
            return null
        }
        val move = moveQueue.poll()
        return if (move == null) {
            val child = children.asSequence().filterNot { it.isTerminalNode }.filterNot { it.isPruned }.maxBy {
                it.score / it.tries + explorationConstant * sqrt(ln(tries.toDouble()) / it.tries.toDouble())
            }
            if (child == null) {
                isPruned = true
            }
            child
        } else {
            val copy = board.copy()
            copy.applyMove(move)
            val movesAndScore = copy.playOut(random)
            val child = Node(copy, explorationConstant, random, this, move, movesAndScore)
            updateScore(movesAndScore.second)
            children.add(child)
            null
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
        val nextMoves = test.moves
        if (nextMoves.isEmpty()) {
            test.moves
        }
        val move = nextMoves.random(random = random)
        test.applyMove(move)
        move
    }.toList()
    return Pair(moves, score ?: throw IllegalStateException())
}

