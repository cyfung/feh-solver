package me.kumatheta.feh

import kotlin.math.abs

data class Position(val x: Int, val y: Int) : Comparable<Position> {
    fun distanceTo(pos: Position): Int {
        return abs(pos.x - x) + abs(pos.y - y)
    }

    override fun compareTo(other: Position): Int {
        return compareValuesBy(
            other,
            this,
            Position::y,
            Position::x
        )
    }

}