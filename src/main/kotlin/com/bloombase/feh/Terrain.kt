package com.bloombase.feh

enum class Terrain {
    WALL,
    PLAIN,
    FOREST,
    MOUNTAIN,
    DEFENSE_TILE,
    TRENCH;

    fun moveCost(moveType: MoveType): Int? {
        return when (this) {
            WALL -> null
            DEFENSE_TILE, PLAIN -> 1
            FOREST -> when (moveType) {
                MoveType.INFANTRY -> 2
                MoveType.CAVALRY -> null
                else -> 1
            }
            MOUNTAIN -> if (moveType == MoveType.FLYING) 1 else null
            TRENCH -> if (moveType == MoveType.CAVALRY) 3 else 1
        }
    }
}