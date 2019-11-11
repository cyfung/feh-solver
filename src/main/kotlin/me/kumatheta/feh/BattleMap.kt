package me.kumatheta.feh

interface BattleMap {
    val size: Position
    fun getTerrain(position: Position): Terrain
    fun toChessPieceMap(): Map<Position, ChessPiece>
}