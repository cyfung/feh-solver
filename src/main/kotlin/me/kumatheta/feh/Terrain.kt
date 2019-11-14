package me.kumatheta.feh

enum class Terrain {
    WALL,
    DEFENSE_TILE,
    FLIER_ONLY,
    FOREST,
    TRENCH,
    REGULAR;

    fun moveCost(moveType: MoveType): Int? {
        return when (this) {
            WALL -> null
            DEFENSE_TILE, REGULAR -> 1
            FOREST -> when (moveType) {
                MoveType.INFANTRY -> 2
                MoveType.CAVALRY -> null
                else -> 1
            }
            FLIER_ONLY -> if (moveType == MoveType.FLYING) 1 else null
            TRENCH -> if (moveType == MoveType.CAVALRY) 3 else 1
        }
    }

    fun priority(moveType: MoveType): Int {
        if (this == WALL || this == DEFENSE_TILE) {
            return -1
        }
        return when (moveType) {
            MoveType.FLYING -> when (this) {
                FLIER_ONLY -> 0
                FOREST -> 1
                else -> 2
            }
            MoveType.ARMORED -> when (this) {
                FOREST -> 0
                else -> 1
            }
            MoveType.CAVALRY -> when (this) {
                TRENCH -> 0
                else -> 1
            }
            MoveType.INFANTRY -> when (this) {
                FOREST -> 0
                else -> 1
            }
        }
    }
}