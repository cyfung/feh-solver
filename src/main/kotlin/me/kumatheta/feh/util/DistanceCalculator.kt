package me.kumatheta.feh.util

import me.kumatheta.feh.BattleMap
import me.kumatheta.feh.ChessPiece
import me.kumatheta.feh.MoveType
import me.kumatheta.feh.Position
import me.kumatheta.feh.Terrain

fun FixedBattleMap.calculateDistance(
    moveType: MoveType,
    startingPosition: Pair<Int, Sequence<MoveStep>>,
    distanceReceiver: DistanceReceiver
) {
    val workingMap = sortedMapOf(
        startingPosition
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

class FixedBattleMap(delegate: BattleMap) : BattleMap {
    val maxPosition = Position(delegate.size.x - 1, delegate.size.y - 1)
    override val size: Position = delegate.size

    override val terrainMap: Map<Position, Terrain> = delegate.terrainMap.toMap()

    private val chessPieceMap = delegate.toChessPieceMap().toMap()
    override fun toChessPieceMap(): Map<Position, ChessPiece> {
        return chessPieceMap
    }

    override val enemyCount: Int = delegate.enemyCount
    override val playerCount: Int = delegate.playerCount

    val distanceMap: Map<Pair<MoveType, Position>, Map<Position, Int>?> by lazy {
        (0 until size.x).flatMap { x -> (0 until size.y).map { y -> Position(x, y) } }.flatMap { position ->
            MoveType.values().map { Pair(it, position) }
        }.associateWith { (moveType, position) ->
            val terrain = getTerrain(position) ?: return@associateWith null
            val startingPosition = 0 to sequenceOf(
                MoveStep(position, terrain, false, 0)
            )
            val resultMap = mutableMapOf<Position, Int>()
            calculateDistance(moveType, startingPosition, object : DistanceReceiver {
                override fun isOverMaxDistance(distance: Int): Boolean {
                    return false
                }

                override fun receive(moveStep: MoveStep): Boolean {
                    return resultMap.putIfAbsent(moveStep.position, moveStep.distanceTravel) == null
                }

            })
            resultMap.toMap()
        }
    }

}

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

