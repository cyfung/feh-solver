package me.kumatheta.mcts

import kotlin.random.Random

interface Board<T : Move> {
    val moves: List<T>
    val score: Long?
    fun applyMove(move: T): Board<T>
    fun suggestedOrder(nextMoves: List<T>): Sequence<T>
    fun getPlayOutMove(random: Random): T
}

interface Move