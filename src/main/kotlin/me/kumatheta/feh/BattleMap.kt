package me.kumatheta.feh

interface BattleMap {
    val size: me.kumatheta.feh.Position
    fun getTerrain(position: me.kumatheta.feh.Position): me.kumatheta.feh.Terrain
    fun toChessPieceMap(): Map<me.kumatheta.feh.Position, me.kumatheta.feh.ChessPiece>
}