package me.kumatheta.feh

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

private val terrainCodes = Terrain.values().associateBy {
    when (it) {
        Terrain.WALL -> 'W'
        Terrain.DEFENSE_TILE -> 'D'
        Terrain.FLIER_ONLY -> 'A'
        Terrain.FOREST -> 'F'
        Terrain.TRENCH -> 'T'
        Terrain.REGULAR -> 'R'
    }
}

enum class SpawnTime {
    START
}

class Spawn(val heroModel: HeroModel, val spawnTime: SpawnTime)

class PositionMap(
    val terrainMap: Map<Position, Terrain>,
    val obstacles: Map<Position, Int>,
    val idMap: Map<Int, Position>,
    val size: Position
)

fun readMap(file: Path): PositionMap {
    val mapLines = Files.readAllLines(file, Charsets.UTF_8)
    if (mapLines.isEmpty()) {
        throw IOException("map csv is empty")
    }
    val sizeData = mapLines[0].split(',')
    if (sizeData.size < 3) {
        throw IOException("map csv wrong format")
    }
    val size = Position(sizeData[1].toInt(), sizeData[2].toInt())
    if (mapLines.size <= size.y) {
        throw IOException("map.csv wrong size, expected size (${size.x}, ${size.y})")
    }
    val obstacles = mutableMapOf<Position, Int>()
    val idMap = mutableMapOf<Int, Position>()
    val terrainMap = mapLines.asSequence().drop(1).mapIndexed { index, s ->
        val y = size.y - index - 1
        val cells = s.split(',')
        if (cells.size != size.x) {
            throw IOException("map.csv wrong size, expected size (${size.x}, ${size.y})")
        }
        cells.asSequence().mapIndexed { x, value ->
            if (value.isEmpty()) {
                throw IOException("cell value too short")
            }
            val position = Position(x, y)
            val terrainCode = value[0].toUpperCase()
            val terrain = if (terrainCode.isLetter()) {
                val terrain = terrainCodes[terrainCode] ?: throw IOException("unknown terrain")
                if (value.length > 1) {
                    value.substring(1).split('.').associateTo(idMap) {
                        it.toInt() to position
                    }
                }
                terrain
            } else {
                val obstacle = value.toInt()
                obstacles[position] = obstacle
                Terrain.REGULAR
            }
            position to terrain
        }
    }.flatMap { it }.associate { it }

    return PositionMap(terrainMap, obstacles.toMap(), idMap.toMap(), size)
}

class FileBattleMap(
    private val positionMap: PositionMap,
    private val spawnMap: Map<Int, Spawn>,
    private val playerMap: Map<Int, HeroModel>
) : BattleMap {
    override val size: Position = positionMap.size

    init {
        val allIdValid = (spawnMap.keys.asSequence() + playerMap.keys.asSequence()).all {
            positionMap.idMap.containsKey(it)
        }
        require(allIdValid) { "invalid id" }
    }

    override fun getTerrain(position: Position): Terrain {
        return positionMap.terrainMap[position] ?: throw IllegalArgumentException("position out of bound")
    }

    override fun toChessPieceMap(): Map<Position, ChessPiece> {
        val chessPieceSequence = spawnMap.asSequence().filter {
            it.value.spawnTime == SpawnTime.START
        }.map {
            val position = positionMap.idMap[it.key] ?: throw IllegalStateException()
            HeroUnit(it.key, it.value.heroModel, Team.ENEMY, position)
        } + positionMap.obstacles.asSequence().map {
            Obstacle(it.value, it.key)
        } + playerMap.asSequence().map {
            val position = positionMap.idMap[it.key] ?: throw IllegalStateException()
            HeroUnit(it.key, it.value, Team.PLAYER, position)
        }

        return chessPieceSequence.associateBy { it.position }
    }
}