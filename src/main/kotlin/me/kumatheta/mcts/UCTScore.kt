package me.kumatheta.mcts

class UCTScore<T : Move>(
    override val totalScore: Long,
    override val tries: Int,
    override val bestScore: Long,
    override val moves: List<T>?
) : Score<T>