package me.kumatheta.feh.util

import com.github.benmanes.caffeine.cache.Caffeine
import me.kumatheta.feh.*

@Deprecated("expired", ReplaceWith("CacheBattleMap"))
class NoCacheBattleMap(delegate: BattleMap) : InternalBattleMap {
    override val maxPosition = Position(delegate.size.x - 1, delegate.size.y - 1)
    override val size: Position = delegate.size

    private val terrainMap: Map<Position, Terrain> = delegate.terrainMap.toMap()
    override val chessPieceMap = delegate.toChessPieceMap().toMap()
    override val reinforceByTime = delegate.reinforceByTime.asSequence().associate { it.key to it.value.toList() }

    override val enemyCount: Int = delegate.enemyCount
    override val playerCount: Int = delegate.playerCount

    override val notWallLocations: Map<Position, Terrain> = terrainMap.filter { it.value.type != Terrain.Type.WALL }
    override fun terrain(index: Position): Terrain {
        return terrainMap[index] ?: throw IllegalArgumentException("out of bound: $index")
    }

    override fun distance(index: DistanceIndex): Map<Position, Int> {
        val (moveType, position, obstacleWalls) = index
        if (obstacleWalls.contains(position)) {
            return emptyMap()
        }
        val terrain = terrain(position)
        if (terrain.moveCost(moveType) == null) {
            return emptyMap()
        }
        val startingPositions = 0 to sequenceOf(
            MoveStep(position, terrain, false, 0)
        )
        return buildDistanceMap(moveType, startingPositions, obstacleWalls)
    }

    override fun chaseTarget(index: ChaseTargetIndex): Map<Position, Int> {
        val (moveType, position, isRanged, obstacleWalls) = index
        val distanceTravel = if (isRanged) 2 else 1
        val startingPositions =
            distanceTravel to attackTargetPositions(position, maxPosition, isRanged).mapNotNull {
                if (obstacleWalls.contains(position)) {
                    return@mapNotNull null
                }
                val attackTerrain = terrain(it)
                if (attackTerrain.moveCost(moveType) == null) {
                    null
                } else {
                    MoveStep(it, attackTerrain, false, distanceTravel)
                }
            }
        return buildDistanceMap(moveType, startingPositions, obstacleWalls)
    }

    override fun threat(index: ThreatIndex): List<MoveStep> {
        val obstacles: Set<Position>
        val obstacleWalls: Set<Position>
        if (index.team == Team.PLAYER) {
            obstacles = index.obstacles
            obstacleWalls = emptySet()
        } else {
            val pair = index.obstacles.partition {
                (chessPieceMap[it] as Obstacle).isBreakableByEnemy
            }
            obstacles = pair.first.toSet()
            obstacleWalls = pair.second.toSet()
        }
        val threatReceiver = ThreatWithoutPass(obstacles)
        val terrain = terrain(index.position)
        val startingPositions = if (terrain.moveCost(index.moveType) != null) {
            0 to sequenceOf(
                MoveStep(index.position, terrain, false, 0)
            )
        } else {
            0 to emptySequence()
        }
        calculateDistance(
            index.moveType,
            startingPositions,
            obstacleWalls,
            threatReceiver
        )
        return threatReceiver.getResult().flatMap { moveStep ->
            attackTargetPositions(moveStep.position, maxPosition, index.isRanged).map {
                moveStep.copy(position = it)
            }
        }.distinct().toList()
    }


    private fun buildDistanceMap(
        moveType: MoveType,
        startingPositions: Pair<Int, Sequence<MoveStep>>,
        obstacleWalls: Set<Position>
    ): Map<Position, Int> {
        val resultMap = mutableMapOf<Position, Int>()
        calculateDistance(moveType, startingPositions, obstacleWalls, object :
            DistanceReceiver {
            override fun isOverMaxDistance(distance: Int): Boolean {
                return false
            }

            override fun receive(moveStep: MoveStep): Boolean {
                return resultMap.putIfAbsent(moveStep.position, moveStep.distanceTravel) == null
            }

        })
        return resultMap.toMap()
    }


}

