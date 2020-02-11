package me.kumatheta.mcts

interface NodeManager<T : Move, S : Score<T>> {

    val estimatedSize: Int

    fun createRootNode(board: Board<T>): Node<T, S>
}
