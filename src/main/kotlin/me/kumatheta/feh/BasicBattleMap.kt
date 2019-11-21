package me.kumatheta.feh

import me.kumatheta.feh.skill.weapon.EmptyWeapon
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

fun readUnits(file: Path): Pair<Map<Int, HeroModel>, Map<Int, Spawn>> {
    val lines = Files.readAllLines(file, Charsets.UTF_8)
    val spawnMap = mutableMapOf<Int, Spawn>()
    val playerMap = mutableMapOf<Int, HeroModel>()
    lines.asSequence().drop(1).map {
        it.split(',')
    }.takeWhile { it.isNotEmpty() && it[0].isNotBlank() }.forEach { values ->
        val valueIterator = values.iterator()
        val id = valueIterator.next().toInt()
        val spawnTimeString = valueIterator.next()
        val spawnTime = if (spawnTimeString.isBlank()) {
            null
        } else {
            SpawnTime.valueOf(spawnTimeString)
        }
        val name = valueIterator.next()
        val groupString = valueIterator.next()
        val group = if (spawnTime == null) {
            groupString.toInt()
        } else {
            0
        }
        val engagedString = valueIterator.next()
        val startEngaged = if (spawnTime == null) {
            engagedString.toBoolean()
        } else {
            true
        }
        val moveType = MoveType.valueOf(valueIterator.next())
        val weaponName = valueIterator.next()
        val weapon = if (weaponName.startsWith('-')) {
            val weaponType = getWeaponType(weaponName.substring(1))
            EmptyWeapon(weaponType)
        } else {
            getWeapon(weaponName)
        }
        val assist = getAssist(valueIterator.next())
        val special = getSpecial(valueIterator.next())
        val stat = Stat(
            valueIterator.next().toInt(),
            valueIterator.next().toInt(),
            valueIterator.next().toInt(),
            valueIterator.next().toInt(),
            valueIterator.next().toInt()
        )
        val passives = valueIterator.asSequence().filterNot {
            it.isBlank()
        }.map {
            getPassive(it)
        }.toList()

        val heroModel = HeroModel(
            name = name,
            group = group,
            startEngaged = startEngaged,
            moveType = moveType,
            weapon = weapon,
            assist = assist,
            special = special,
            stat = stat,
            passives = passives
        )
        if (spawnTime == null) {
            playerMap[id] = heroModel
        } else {
            spawnMap[id] = Spawn(heroModel, spawnTime)
        }
    }
    return Pair(playerMap.toMap(), spawnMap.toMap())
}

private fun getWeaponType(name: String): WeaponType {
    return getObject("me.kumatheta.feh.$name") {
        "weapon type not found $name"
    }
}

private fun getWeapon(name: String): Weapon {
    return getSkill(name, "weapon")
}

private fun getAssist(name: String): Assist? {
    if (name.isBlank()) return null
    return getSkill(name, "assist")
}

private fun getSpecial(name: String): Special? {
    if (name.isBlank()) return null
    return getSkill(name, "special")
}

private fun getPassive(name: String): Passive {
    return getSkill(name, "passive")
}

private fun <T> getSkill(name: String, type: String): T {
    return getObject("me.kumatheta.feh.skill.$type.$name") {
        "$type not found $name"
    }
}

private inline fun <T> getObject(className: String, crossinline messageCreator: () -> String): T {
    val clazz = Class.forName(className).kotlin
    val instance = clazz.objectInstance
    @Suppress("UNCHECKED_CAST")
    return instance as? T ?: throw IOException(
        messageCreator()
    )
}

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

class BasicBattleMap(
    positionMap: PositionMap,
    spawnMap: Map<Int, Spawn>,
    playerMap: Map<Int, HeroModel>
) : BattleMap {
    private val spawnMap = spawnMap.toMap()
    private val playerMap = playerMap.toMap()
    override val size: Position = positionMap.size
    private val terrainMap = positionMap.terrainMap.toMap()
    private val obstacles = positionMap.obstacles.toMap()
    private val idMap = positionMap.idMap.toMap()

    init {
        val invalidId = (spawnMap.keys.asSequence() + playerMap.keys.asSequence()).filterNot {
            positionMap.idMap.containsKey(it)
        }.firstOrNull()
        require(invalidId == null) { "invalid id $invalidId" }
    }

    override fun getTerrain(position: Position): Terrain {
        return terrainMap[position] ?: throw IllegalArgumentException("position out of bound")
    }

    override fun toChessPieceMap(): Map<Position, ChessPiece> {
        val chessPieceSequence = spawnMap.asSequence().filter {
            it.value.spawnTime == SpawnTime.START
        }.map {
            val position = idMap[it.key] ?: throw IllegalStateException()
            HeroUnit(it.key, it.value.heroModel, Team.ENEMY, position)
        } + obstacles.asSequence().map {
            Obstacle(it.value, it.key)
        } + playerMap.asSequence().map {
            val position = idMap[it.key] ?: throw IllegalStateException()
            HeroUnit(it.key, it.value, Team.PLAYER, position)
        }

        return chessPieceSequence.associateBy { it.position }
    }
}