package me.kumatheta.feh

import me.kumatheta.feh.skill.*
import me.kumatheta.feh.skill.weapon.EmptyWeapon
import java.nio.file.Files
import java.nio.file.Path

sealed class SpawnTime

object Unknown : SpawnTime()
object Start : SpawnTime()
class ReinforceByTime(val turn: Int) : SpawnTime()

class Spawn(val heroModel: HeroModel, val cooldown: Int?, val spawnTime: SpawnTime)

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
            when (val time = spawnTimeString.toIntOrNull()) {
                null -> Unknown
                1 -> Start
                else -> ReinforceByTime(time)
            }
        }
        val name = valueIterator.next()
        val imageName = valueIterator.next()
        val groupString = valueIterator.next()
        val engagedString = valueIterator.next()
        val cdString = valueIterator.next()
        val group: Int?
        val engageDelay: Int?
        val cooldown: Int?
        if (spawnTime == null) {
            group = null
            engageDelay = null
            cooldown = null
        } else {
            group = groupString.toIntOrNull()
            engageDelay = engagedString.toIntOrNull()
            cooldown = cdString.toIntOrNull()
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
            imageName = imageName,
            group = group,
            engageDelay = engageDelay,
            moveType = moveType,
            weapon = weapon,
            assist = assist,
            special = special,
            stat = stat,
            passives = passives,
            isFinalStat = true
        )
        if (spawnTime == null) {
            playerMap[id] = heroModel
        } else {
            spawnMap[id] = Spawn(heroModel, cooldown, spawnTime)
        }
    }
    return Pair(playerMap.toMap(), spawnMap.toMap())
}

enum class WeaponTypeName(val weaponType: WeaponType) {
    SWORD(Sword),
    LANCE(Lance),
    AXE(Axe)
}

private fun getWeaponType(name: String): WeaponType {
    return WeaponTypeName.valueOf(name).weaponType
}

private fun String.splitOrNull(delimiter: Char): Pair<String, String?> {
    val index = indexOf(delimiter)
    return if (index == -1) {
        Pair(this, null)
    } else {
        Pair(this.substring(0, index), this.substring(index + 1))
    }
}


private fun getWeapon(name: String): Weapon {
    val (baseName, color: String?) = name.splitOrNull('@')
    val (weaponName, baseRefine: String?) = baseName.splitOrNull('*')
    val weapon = if (baseRefine != null) {
        val trimBaseRefine = baseRefine.trim()
        if (trimBaseRefine.length != 1) {
            throw IllegalArgumentException("baseRefine can only be one of A,S,D,R")
        }
        val spdRefineWeapon = ALL_WEAPONS["$weaponName*S"]
        val refineType = trimBaseRefine[0]
        if (refineType == 'S') {
            spdRefineWeapon
        } else {
            val spdRefineExtraStat = spdRefineWeapon.extraStat!!
            when (refineType) {
                'A' -> spdRefineWeapon.copy(
                    basicSkill = spdRefineWeapon.basicSkill.copy(
                        extraStat = spdRefineExtraStat.copy(
                            spd = 0,
                            atk = spdRefineExtraStat.atk + spdRefineExtraStat.spd - 1
                        )
                    )
                )
                'D' -> spdRefineWeapon.copy(
                    basicSkill = spdRefineWeapon.basicSkill.copy(
                        extraStat = spdRefineExtraStat.copy(spd = 0, def = spdRefineExtraStat.spd + 1)
                    )
                )
                'R' -> spdRefineWeapon.copy(
                    basicSkill = spdRefineWeapon.basicSkill.copy(
                        extraStat = spdRefineExtraStat.copy(spd = 0, res = spdRefineExtraStat.spd + 1)
                    )
                )
                else -> throw IllegalArgumentException("color can only be one of A,S,D,R")
            }
        }
    } else {
        ALL_WEAPONS[weaponName]
    }
    return if (color != null) {
        val weaponColor = when (color.trim()) {
            "R" -> Color.RED
            "G" -> Color.GREEN
            "B" -> Color.BLUE
            "C" -> Color.COLORLESS
            else -> throw IllegalArgumentException("wrong weapon name $name, color can only be one of R,G,B,C")
        }
        weapon.copy(weaponType = (weapon.weaponType as FreeColorWeapon).toColor(weaponColor))
    } else {
        weapon
    }
}

private fun getAssist(name: String): Assist? {
    if (name.isBlank()) return null
    return ALL_ASSISTS[name]
}

private fun getSpecial(name: String): Special? {
    if (name.isBlank()) return null
    return ALL_SPECIALS[name]
}

private fun getPassive(name: String): Passive {
    return ALL_PASSIVES[name]
}

class BasicBattleMap(
    positionMap: PositionMap,
    spawnMap: Map<Int, Spawn>,
    playerMap: Map<Int, HeroModel>
) : BattleMap {
    override val enemyCount = spawnMap.size
    override val playerCount = playerMap.size
    private val spawnMap = spawnMap.toMap()
    private val playerMap = playerMap.toMap()
    override val size: Position = positionMap.size
    override val terrainMap = positionMap.terrainMap.toMap()
    private val obstacles = positionMap.obstacles.toMap()
    private val idMap = positionMap.idMap.toMap()

    init {
        val invalidId = (spawnMap.keys.asSequence() + playerMap.keys.asSequence()).filterNot {
            positionMap.idMap.containsKey(it)
        }.firstOrNull()
        require(invalidId == null) { "invalid id $invalidId" }
    }

    override fun toChessPieceMap(): Map<Position, ChessPiece> {
        val chessPieceSequence = spawnMap.asSequence().filter {
            it.value.spawnTime == Start
        }.map {
            newHeroUnit(it.key, it.value)
        } + obstacles.asSequence().map {
            val value = it.value
            if (value > 4 || value < -2) {
                throw IllegalStateException("invalid obstacle value $value")
            } else if (value > 2) {
                Obstacle(value - 2, it.key, false)
            } else {
                Obstacle(value, it.key, true)
            }
        } + playerMap.asSequence().map {
            val position = idMap[it.key] ?: throw IllegalStateException()
            HeroUnit(it.key, it.value, Team.PLAYER, position)
        }

        return chessPieceSequence.associateBy { it.position }
    }

    private fun newHeroUnit(id: Int, spawn: Spawn): HeroUnit {
        val position = idMap[id] ?: throw IllegalStateException()
        return HeroUnit(
            id = id,
            heroModel = spawn.heroModel,
            team = Team.ENEMY,
            position = position,
            cooldown = spawn.cooldown
        )
    }

    override val reinforceByTime
        get() = spawnMap.asSequence().mapNotNull {
            val spawn = it.value
            val spawnTime = spawn.spawnTime
            if (spawnTime is ReinforceByTime) {
                spawnTime.turn to newHeroUnit(it.key, spawn)
            } else {
                null
            }
        }.groupBy({
            it.first
        }, {
            it.second
        })
}