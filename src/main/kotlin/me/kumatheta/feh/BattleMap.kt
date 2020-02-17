package me.kumatheta.feh

interface BattleMap {
    val size: Position
    val terrainMap: Map<Position, Terrain>
    fun toChessPieceMap(): Map<Position, ChessPiece>
    val enemyCount: Int
    val playerCount: Int
}