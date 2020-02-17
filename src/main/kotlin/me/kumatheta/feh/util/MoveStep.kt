package me.kumatheta.feh.util

import me.kumatheta.feh.Position
import me.kumatheta.feh.Terrain

data class MoveStep(
    val position: Position,
    val terrain: Terrain,
    val teleportRequired: Boolean,
    val distanceTravel: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MoveStep) return false

        if (position != other.position) return false

        return true
    }

    override fun hashCode(): Int {
        return position.hashCode()
    }
}