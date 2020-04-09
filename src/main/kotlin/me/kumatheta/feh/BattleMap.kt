package me.kumatheta.feh

import me.kumatheta.feh.util.ChaseTargetIndex
import me.kumatheta.feh.util.DistanceIndex
import me.kumatheta.feh.util.MoveStep
import me.kumatheta.feh.util.ThreatIndex

interface BattleMap {
    val size: Position
    val terrainMap: Map<Position, Terrain>
    fun toChessPieceMap(playerUnits: List<HeroUnit>): Map<Position, ChessPiece>
    val enemyCount: Int
    val playerCount: Int
    val reinforceByTime: Map<Int, List<HeroUnit>>
}

interface InternalBattleMap {
    val engaged: Boolean
    val notWallLocations: Map<Position, Terrain>
    val maxPosition: Position
    val size: Position
    val enemyCount: Int
    val playerCount: Int
    val chessPieceMap: Map<Position, ChessPiece>
    val reinforceByTime: Map<Int, List<HeroUnit>>
    fun terrain(index: Position): Terrain
    fun distance(index: DistanceIndex): Map<Position, Int>
    fun chaseTarget(index: ChaseTargetIndex): Map<Position, Int>
    fun threat(index: ThreatIndex): List<MoveStep>
}

fun InternalBattleMap.chaseTarget(
    moveType: MoveType,
    position: Position,
    isRanged: Boolean,
    obstacleWalls: Set<Position>
) = chaseTarget(ChaseTargetIndex(
    moveType = moveType,
    position = position,
    isRanged = isRanged,
    obstacleWalls = obstacleWalls
))

fun InternalBattleMap.distance(
    moveType: MoveType,
    position: Position,
    obstacleWalls: Set<Position>
) = distance(DistanceIndex(
    moveType = moveType,
    position = position,
    obstacleWalls = obstacleWalls
))

fun InternalBattleMap.threat(
    moveType: MoveType,
    position: Position,
    isRanged: Boolean,
    obstacles: Set<Position>,
    team: Team
) = threat(ThreatIndex(
    moveType = moveType,
    position = position,
    isRanged = isRanged,
    obstacles = obstacles,
    team = team
))