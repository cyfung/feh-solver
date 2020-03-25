package me.kumatheta.feh.util

import com.github.benmanes.caffeine.cache.Caffeine
import me.kumatheta.feh.*

class CacheBattleMap(
    delegate: BattleMap,
    playerUnits: List<HeroUnit>
) : InternalBattleMap {
    override val maxPosition = Position(delegate.size.x - 1, delegate.size.y - 1)
    override val size: Position = delegate.size

    private val terrainMap: Map<Position, Terrain> = delegate.terrainMap.toMap()
    override val chessPieceMap = delegate.toChessPieceMap(playerUnits).toMap()
    override val reinforceByTime = delegate.reinforceByTime.asSequence().associate { it.key to it.value.toList() }

    override val enemyCount: Int = delegate.enemyCount
    override val playerCount: Int = delegate.playerCount

    override val notWallLocations: Map<Position, Terrain> = terrainMap.filter { it.value.type != Terrain.Type.WALL }
    override fun terrain(index: Position): Terrain {
        return terrainMap[index] ?: throw IllegalArgumentException("out of bound: $index")
    }

    override fun distance(index: DistanceIndex): Map<Position, Int> {
        return checkNotNull(distanceCache[index])
    }

    override fun chaseTarget(index: ChaseTargetIndex): Map<Position, Int> {
        return checkNotNull(chaseAttackCache[index])
    }

    override fun threat(index: ThreatIndex): List<MoveStep> {
        return checkNotNull(threatCache[index])
    }

    private val distanceCache =
        Caffeine.newBuilder().build<DistanceIndex, Map<Position, Int>> { (moveType, position, obstacleWalls) ->
            if (obstacleWalls.contains(position)) {
                return@build emptyMap<Position, Int>()
            }
            val terrain = terrain(position)
            if (terrain.moveCost(moveType) == null) {
                return@build emptyMap<Position, Int>()
            }
            val startingPositions = 0 to sequenceOf(
                MoveStep(position, terrain, false, 0)
            )
            buildDistanceMap(moveType, startingPositions, obstacleWalls)
        }

    private val chaseAttackCache = Caffeine.newBuilder()
        .build<ChaseTargetIndex, Map<Position, Int>> { (moveType, position, isRanged, obstacleWalls) ->
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
            buildDistanceMap(moveType, startingPositions, obstacleWalls)
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

    private val threatCache = Caffeine.newBuilder()
        .build<ThreatIndex, List<MoveStep>> { threatIndex ->
            val obstacles: Set<Position>
            val obstacleWalls: Set<Position>
            if (threatIndex.team == Team.PLAYER) {
                obstacles = threatIndex.obstacles
                obstacleWalls = emptySet()
            } else {
                val pair = threatIndex.obstacles.partition {
                    (chessPieceMap[it] as Obstacle).isBreakableByEnemy
                }
                obstacles = pair.first.toSet()
                obstacleWalls = pair.second.toSet()
            }
            val threatReceiver = ThreatWithoutPass(obstacles)
            val terrain = terrain(threatIndex.position)
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
                obstacleWalls,
                threatReceiver
            )
            threatReceiver.getResult().flatMap { moveStep ->
                attackTargetPositions(moveStep.position, maxPosition, threatIndex.isRanged).map {
                    moveStep.copy(position = it)
                }
            }.distinct().toList()
        }


}

