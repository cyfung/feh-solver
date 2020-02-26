package me.kumatheta.feh

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

private val terrainCodes = Terrain.Type.values().associateBy {
    when (it) {
        Terrain.Type.WALL -> 'W'
        Terrain.Type.FLIER_ONLY -> 'A'
        Terrain.Type.FOREST -> 'F'
        Terrain.Type.TRENCH -> 'T'
        Terrain.Type.REGULAR -> 'R'
    }
}

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
            throw IOException("map csv wrong size, expected size (${size.x}, ${size.y})")
        }
        cells.asSequence().mapIndexed { x, value ->
            if (value.isEmpty()) {
                throw IOException("cell value too short")
            }
            val position = Position(x, y)
            val idStart: Int
            val isDefenseTile: Boolean
            val temp = value[0].toUpperCase()
            val terrainCode = if (temp == 'D') {
                isDefenseTile = true
                idStart = 2
                value[1].toUpperCase()
            } else {
                isDefenseTile = false
                idStart = 1
                temp
            }

            val terrainType = when {
                terrainCode.isLetter() -> {
                    val terrain = terrainCodes[terrainCode] ?: throw IOException("unknown terrain")
                    if (value.length > 1) {
                        value.substring(idStart).split('.').associateTo(idMap) {
                            it.toInt() to position
                        }
                    }
                    terrain
                }
                else -> {
                    val obstacle = value.toInt()
                    obstacles[position] = obstacle
                    Terrain.Type.REGULAR
                }
            }
            position to Terrain(terrainType, isDefenseTile)
        }
    }.flatMap { it }.associate { it }

    return PositionMap(terrainMap, obstacles.toMap(), idMap.toMap(), size)
}
