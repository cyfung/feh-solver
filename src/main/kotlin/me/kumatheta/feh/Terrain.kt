package me.kumatheta.feh

data class Terrain(val type: Type, val isDefenseTile: Boolean) {

    enum class Type {
        WALL,
        FLIER_ONLY,
        FOREST,
        TRENCH,
        REGULAR;

        fun moveCost(moveType: MoveType): Int? {
            return when (this) {
                WALL -> null
                REGULAR -> 1
                FOREST -> when (moveType) {
                    MoveType.INFANTRY -> 2
                    MoveType.CAVALRY -> null
                    else -> 1
                }
                FLIER_ONLY -> if (moveType == MoveType.FLYING) 1 else null
                TRENCH -> if (moveType == MoveType.CAVALRY) 3 else 1
            }
        }
    }

    fun moveCost(moveType: MoveType): Int? {
        return type.moveCost(moveType)
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