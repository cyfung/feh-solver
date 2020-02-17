package me.kumatheta.feh.util

import me.kumatheta.feh.BattleMap
import me.kumatheta.feh.MoveType
import me.kumatheta.feh.Position
import me.kumatheta.feh.Terrain

fun FixedBattleMap.calculateDistance(
    moveType: MoveType,
    startingPositions: Pair<Int, Sequence<MoveStep>>,
    distanceReceiver: DistanceReceiver
) {
    val workingMap = sortedMapOf(
        startingPositions
    )

    while (workingMap.isNotEmpty()) {
        val currentDistance = workingMap.firstKey()
        if (distanceReceiver.isOverMaxDistance(currentDistance)) {
            break
        }
        val temp = workingMap.remove(currentDistance) ?: throw IllegalStateException()
        temp.asSequence().filter {
            distanceReceiver.receive(it)
        }.flatMap { it.position.surroundings(maxPosition) }.mapNotNull { position ->
            val terrain = getTerrain(position)
            val moveCost = terrain.moveCost(moveType) ?: return@mapNotNull null
            val distanceTravel = currentDistance + moveCost
            distanceTravel to MoveStep(position, terrain, false, distanceTravel)
        }.groupBy({ it.first }, { it.second }).forEach { (distance, list) ->
            workingMap.add(distance, list.asSequence())
        }
    }
}

data class DistanceIndex(val moveType: MoveType, val position: Position, val isRanged: Boolean)

data class ThreatIndex(val moveType: MoveType, val position: Position, val isRanged: Boolean, val obstacles: Set<Position>)

interface DistanceReceiver {
    fun isOverMaxDistance(distance: Int): Boolean
    fun receive(moveStep: MoveStep): Boolean
}

fun BattleMap.getTerrain(position: Position): Terrain {
    return terrainMap[position] ?: throw IllegalArgumentException("out of bound: $position")
}

private fun <K, V> MutableMap<K, Sequence<V>>.add(k: K, values: Sequence<V>) {
    val v = this[k]
    this[k] = if (v == null) {
        values
    } else {
        v + values
    }
}

