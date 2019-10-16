package com.bloombase.feh

interface BattleMap {
    val size: Position
    fun toChessPieceMap(): Map<Position, ChessPiece>
}