package me.kumatheta.feh.util

import me.kumatheta.feh.BattleMap
import me.kumatheta.feh.ChessPiece
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

    val distanceMap: Map<DistanceIndex, Map<Position, Int>> by lazy {
        (0 until size.x).asSequence()
            .flatMap { x -> (0 until size.y).asSequence().map { y -> Position(x, y) } }
            .flatMap { position ->
                MoveType.values().asSequence()
                    .flatMap { sequenceOf(DistanceIndex(it, position, true), DistanceIndex(it, position, false)) }
            }.associateWith { (moveType, position, isRanged) ->
                val terrain = getTerrain(position)
                val startingPositions = if (terrain.moveCost(moveType) != null) {
                    0 to sequenceOf(
                        MoveStep(position, terrain, false, 0)
                    )
                } else {
                    val distanceTravel = if (isRanged) 2 else 1
                    distanceTravel to attackTargetPositions(position, maxPosition, isRanged).mapNotNull {
                        val attackTerrain = getTerrain(it)
                        if (attackTerrain.moveCost(moveType) == null) {
                            null
                        } else {
                            MoveStep(it, attackTerrain, false, distanceTravel)
                        }
                    }
                }
                val resultMap = mutableMapOf<Position, Int>()
                calculateDistance(moveType, startingPositions, object : DistanceReceiver {
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

