package me.kumatheta.feh.util

import com.marcinmoskala.math.powerset
import me.kumatheta.feh.BattleMap
import me.kumatheta.feh.ChessPiece
import me.kumatheta.feh.MoveType
import me.kumatheta.feh.Obstacle
import me.kumatheta.feh.Position
import me.kumatheta.feh.Terrain
import me.kumatheta.feh.ThreatReceiver

class FixedBattleMap(delegate: BattleMap) :
    BattleMap {
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
            .flatMap { x ->
                (0 until size.y).asSequence().map { y ->
                    Position(x, y)
                }
            }
            .flatMap { position ->
                MoveType.values().asSequence()
                    .flatMap {
                        sequenceOf(
                            DistanceIndex(it, position, true),
                            DistanceIndex(it, position, false)
                        )
                    }
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
                calculateDistance(moveType, startingPositions, object :
                    DistanceReceiver {
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

    val threatMap by lazy {
        chessPieceMap.values.filterIsInstance<Obstacle>().map { it.position }.powerset().asSequence()
            .flatMap { obstacles ->
                (0 until size.x).asSequence()
                    .flatMap { x ->
                        (0 until size.y).asSequence().map { y ->
                            Position(x, y)
                        }
                    }
                    .flatMap { position ->
                        MoveType.values().asSequence()
                            .flatMap {
                                sequenceOf(
                                    ThreatIndex(it, position, true, obstacles),
                                    ThreatIndex(it, position, false, obstacles)
                                )
                            }
                    }
            }.associateWith { threatIndex ->
                val threatReceiver = ThreatWithoutPass(threatIndex.obstacles)
                val terrain = getTerrain(threatIndex.position)
                val startingPositions = if (terrain.moveCost(threatIndex.moveType) != null) {
                    0 to sequenceOf(
                        MoveStep(threatIndex.position, terrain, false, 0)
                    )
                } else {
                    0 to emptySequence()
                }
                calculateDistance(
                    threatIndex.moveType,
                    startingPositions,
                    threatReceiver
                )
                threatReceiver.getResult().flatMap { moveStep ->
                    attackTargetPositions(moveStep.position, maxPosition, threatIndex.isRanged).map {
                        moveStep.copy(position = it)
                    }
                }.distinct().toList()
            }
    }

}

private class ThreatWithoutPass(
    private val obstacles: Set<Position>
) : ThreatReceiver(3) {
    override fun receive(moveStep: MoveStep): Boolean {
        if (resultMap[moveStep.position] != null) {
            return false
        }
        val isNotObstacle = !obstacles.contains(moveStep.position)
        resultMap[moveStep.position] = Pair(isNotObstacle, moveStep)
        return isNotObstacle
    }

    fun getResult() = resultMap.values.asSequence().filter { it.first }.map { it.second }
}