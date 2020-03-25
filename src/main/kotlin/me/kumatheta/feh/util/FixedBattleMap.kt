package me.kumatheta.feh.util

import com.marcinmoskala.math.powerset
import me.kumatheta.feh.BattleMap
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.InternalBattleMap
import me.kumatheta.feh.MoveType
import me.kumatheta.feh.Obstacle
import me.kumatheta.feh.Position
import me.kumatheta.feh.Team
import me.kumatheta.feh.Terrain

class FixedBattleMap(
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
        return requireNotNull(distanceMap[index])
    }

    override fun chaseTarget(index: ChaseTargetIndex): Map<Position, Int> {
        return requireNotNull(chaseAttackMap[index])
    }

    override fun threat(index: ThreatIndex): List<MoveStep> {
        return requireNotNull(threatMap[index])
    }

    private val distanceMap: Map<DistanceIndex, Map<Position, Int>> by lazy {
        distanceIndexes().associateWith { (moveType, position, obstacleWalls) ->
            if (obstacleWalls.contains(position)) {
                return@associateWith emptyMap<Position, Int>()
            }
            val terrain = terrain(position)
            if (terrain.moveCost(moveType) == null) {
                return@associateWith emptyMap<Position, Int>()
            }
            val startingPositions = 0 to sequenceOf(
                MoveStep(position, terrain, false, 0)
            )
            buildDistanceMap(moveType, startingPositions, obstacleWalls)
        }
    }

    private val chaseAttackMap: Map<ChaseTargetIndex, Map<Position, Int>> by lazy {
        chaseTargetIndex().associateWith { (moveType, position, isRanged, obstacleWalls) ->
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
    }

    private fun distanceIndexes(): Sequence<DistanceIndex> {
        return chessPieceMap.values.filterIsInstance<Obstacle>().filterNot { it.isBreakableByEnemy }
            .map { it.position }.powerset().asSequence()
            .flatMap { obstacleWalls ->
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
                                    DistanceIndex(it, position, obstacleWalls)
                                )
                            }
                    }
            }
    }

    private fun chaseTargetIndex(): Sequence<ChaseTargetIndex> {
        return chessPieceMap.values.filterIsInstance<Obstacle>().filterNot { it.isBreakableByEnemy }
            .map { it.position }.powerset().asSequence()
            .flatMap { obstacleWalls ->
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
                                    ChaseTargetIndex(it, position, true, obstacleWalls),
                                    ChaseTargetIndex(it, position, false, obstacleWalls)
                                )
                            }
                    }
            }
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

    private val threatMap by lazy {
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
                                    ThreatIndex(it, position, true, obstacles, Team.PLAYER),
                                    ThreatIndex(it, position, false, obstacles, Team.PLAYER),
                                    ThreatIndex(it, position, true, obstacles, Team.ENEMY),
                                    ThreatIndex(it, position, false, obstacles, Team.ENEMY)
                                )
                            }
                    }
            }.associateWith { threatIndex ->
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

}