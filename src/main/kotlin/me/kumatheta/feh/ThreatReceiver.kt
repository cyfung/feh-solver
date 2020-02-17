package me.kumatheta.feh

import me.kumatheta.feh.util.DistanceReceiver
import me.kumatheta.feh.util.MoveStep

abstract class ThreatReceiver(private val movementRange: Int) : DistanceReceiver,
    ThreatMoves {
    protected val resultMap = mutableMapOf<Position, Pair<Boolean, MoveStep>>()
    final override val movablePositions
        get() = resultMap.asSequence().filter { it.value.first }.map { it.key }

    override fun isOverMaxDistance(distance: Int): Boolean {
        return distance > movementRange
    }
}