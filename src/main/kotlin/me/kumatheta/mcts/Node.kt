package me.kumatheta.mcts

interface Node<T : Move> {
    val bestScore: Double
    val tries: Int

    fun getBestChild(): Node<T>?
    fun selectAndPlayOut(): Node<T>?
    val playOutMove: List<T>?
    val lastMove: T?
}

fun <T: Move> Node<T>.getBestMoves(): List<T> {
    val bestRoute = generateSequence(this) {
        it.getBestChild()
    }.toList()

    return (bestRoute.asSequence().mapNotNull {
        it.lastMove
    } + (bestRoute.last().playOutMove ?: throw IllegalStateException())).toList()
}