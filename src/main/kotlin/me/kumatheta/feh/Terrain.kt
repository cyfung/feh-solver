package me.kumatheta.feh

class Terrain(val type: Type, val isDefenseTile: Boolean) {

    enum class Type {
        WALL,
        FLIER_ONLY,
        FOREST,
        TRENCH,
        REGULAR;
    }

    fun moveCost(moveType: MoveType): Int? {
        return when (type) {
            Type.WALL -> null
            Type.REGULAR -> 1
            Type.FOREST -> when (moveType) {
                MoveType.INFANTRY -> 2
                MoveType.CAVALRY -> null
                else -> 1
            }
            Type.FLIER_ONLY -> if (moveType == MoveType.FLYING) 1 else null
            Type.TRENCH -> if (moveType == MoveType.CAVALRY) 3 else 1
        }
    }

    fun priority(moveType: MoveType): Int {
        if (type == Type.WALL) {
            return -1
        }
        return when (moveType) {
            MoveType.FLYING -> when (type) {
                Type.FLIER_ONLY -> 0
                Type.FOREST -> 1
                else -> 2
            }
            MoveType.ARMORED -> when (type) {
                Type.FOREST -> 0
                else -> 1
            }
            MoveType.CAVALRY -> when (type) {
                Type.TRENCH -> 0
                else -> 1
            }
            MoveType.INFANTRY -> when (type) {
                Type.FOREST -> 0
                else -> 1
            }
        }
    }
}