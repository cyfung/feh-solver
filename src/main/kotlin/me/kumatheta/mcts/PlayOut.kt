package me.kumatheta.mcts

import kotlin.random.Random

fun <T : Move> Board<T>.playOut(random: Random): Pair<Long, List<T>> {
    var temp = this.score
    if (temp != null) {
        throw IllegalArgumentException()
    }
    var test = this
    val moves = generateSequence {
        temp = test.score
        if (temp != null) {
            return@generateSequence null
        }
        val nextMoves = test.moves
        if (nextMoves.isEmpty()) {
            throw IllegalStateException()
        }
        val move = test.getPlayOutMove(random = random)
        test = test.applyMove(move)
        move
    }.toList()
    val score = temp
    require(score != null)
    return score to moves
}