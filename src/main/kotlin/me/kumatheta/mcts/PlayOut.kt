package me.kumatheta.mcts

import kotlin.random.Random

fun <T : Move> Board<T>.playOut(random: Random): Pair<List<T>, Double> {
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