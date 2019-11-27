package me.kumatheta.mcts

import kotlin.random.Random

fun <T : Move> Board<T>.playOut(random: Random): Score<T> {
    var temp = this.score
    if (temp != null) {
        throw IllegalArgumentException()
    }
    val test = copy()
    val moves = generateSequence {
        temp = test.score
        if (temp != null) {
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
    val score = temp
    require(score != null)
    return Score(score, 1, score, moves)
}