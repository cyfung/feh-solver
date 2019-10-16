package com.bloombase.feh

import kotlin.math.abs

data class Position(val x: Int, val y: Int) {
    fun distanceTo(pos: Position): Int {
        return abs(pos.x - x) + abs(pos.y - y)
    }
}