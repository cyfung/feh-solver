package me.kumatheta.mcts

interface NodeManager<T : Move, S : Score<T>> {

    val estimatedSize: Int

    fun createRootNode(board: Board<T>, emptyScore: S): Node<T, S>
}
