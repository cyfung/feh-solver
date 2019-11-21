package me.kumatheta.feh

import java.io.FileNotFoundException
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
    PLAYER, // always spawn first turn
    FIRST_TURN
}

class Spawn(val heroModel: HeroModel, val spawnTime: SpawnTime, val position: Position)

class FileBattleMap(folder: Path) : BattleMap {
    private val terrainMap: Map<Position, Terrain>
    private val spawnMap: Map<Int, Spawn>
    override val size: Position

    init {
        if (Files.isDirectory(folder)) {
            throw FileNotFoundException("$folder is not a directory")
        }
        val map = folder.resolve("map.csv")
        size = Position(6, 8)
        val mapLines = Files.readAllLines(map, Charsets.UTF_8)
        if (mapLines.size < size.y) {
            throw IOException("map.csv wrong size, expected size (${size.x}, ${size.y})")
        }
        val obstacles = mutableMapOf<Position, Int>()
        terrainMap = mapLines.asSequence().mapIndexed { index, s ->
            val y = size.y - index - 1
            val cells = s.split(",")
            if (cells.size != size.x) {
                throw IOException("map.csv wrong size, expected size (${size.x}, ${size.y})")
            }
            cells.asSequence().mapIndexed { x, value ->
                if (value.isEmpty()) {
                    throw IOException("cell value too short")
                }
                val terrainCode = value[0]
                val terrain = terrainCodes[terrainCode] ?: throw IOException("illegal terrain code $terrainCode")
                val obstacle = value.substring(1).toIntOrNull()
                val position = Position(x, y)
                if (obstacle != null) {
                    if(terrain != Terrain.REGULAR) {
                        throw IOException("illegal terrain only regular terrain can have obstacle")
                    }
                    obstacles[position] = obstacle
                }
                position to terrain
            }
        }.flatMap { it }.associate { it }
        spawnMap = mapOf()
    }

    override fun getTerrain(position: Position): Terrain {
        return terrainMap[position] ?: throw IllegalArgumentException("position out of bound")
    }

    override fun toChessPieceMap(): Map<Position, ChessPiece> {
        return spawnMap.asSequence().filter {
            it.value.spawnTime == SpawnTime.FIRST_TURN || it.value.spawnTime == SpawnTime.PLAYER
        }.associate {
            val team = if(it.value.spawnTime == SpawnTime.PLAYER) Team.PLAYER else Team.ENEMY
            it.value.position to HeroUnit(it.key, it.value.heroModel, team)
        }
    }
}