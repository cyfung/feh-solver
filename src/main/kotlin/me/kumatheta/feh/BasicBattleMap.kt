package me.kumatheta.feh

import me.kumatheta.feh.skill.ALL_ASSISTS
import me.kumatheta.feh.skill.ALL_PASSIVES
import me.kumatheta.feh.skill.ALL_SPECIALS
import me.kumatheta.feh.skill.ALL_WEAPONS
import me.kumatheta.feh.skill.Assist
import me.kumatheta.feh.skill.BASIC_REFINABLE_WEAPONS
import me.kumatheta.feh.skill.Passive
import me.kumatheta.feh.skill.Special
import me.kumatheta.feh.skill.Weapon
import me.kumatheta.feh.skill.effect.DisableFoeCounter
import me.kumatheta.feh.skill.effect.SkillEffect
import me.kumatheta.feh.skill.effect.StaffAsNormalBasic
import me.kumatheta.feh.skill.getBasicRefine
import me.kumatheta.feh.skill.plus
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
        val name = valueIterator.next()
        val imageName = valueIterator.next()
        val moveType = MoveType.valueOf(valueIterator.next())
        val stat = Stat(
            valueIterator.next().toInt(),
            valueIterator.next().toInt(),
            valueIterator.next().toInt(),
            valueIterator.next().toInt(),
            valueIterator.next().toInt()
        )
        val weaponName = valueIterator.next()
        val refine = valueIterator.next()
        val colorChange = valueIterator.next()
        val weapon = if (weaponName.startsWith('-')) {
            val weaponType = getWeaponType(weaponName.substring(1))
            EmptyWeapon(weaponType)
        } else {
            getWeapon(weaponName, refine, colorChange)
        }
        val assist = getAssist(valueIterator.next())
        val special = getSpecial(valueIterator.next())
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

sealed class Refine(val name: String) {
}

class BaseRefine(val statType: StatType) : Refine(statType.name)

object SpecialRefine : Refine("special")

class StaffRefine(name: String, val skillEffect: SkillEffect) : Refine(name)


val refineMap = (statSequence { BaseRefine(it) } + SpecialRefine +
        StaffRefine("wrathful", StaffAsNormalBasic) +
        StaffRefine("dazzling", DisableFoeCounter)).associateBy { it.name.toLowerCase() }


private fun getWeapon(weaponName: String, refine: String, colorChange: String): Weapon {
    val weapon = when (val actualRefine = refineMap[refine.trim().toLowerCase()]) {
        null -> {
            check(refine.trim().isEmpty()) {
                "refine can only be one of ${refineMap.keys}"
            }
            ALL_WEAPONS[weaponName]
        }
        SpecialRefine -> ALL_WEAPONS["$weaponName Eff"]
        is BaseRefine -> {
            val refinableWeapon = BASIC_REFINABLE_WEAPONS[weaponName]
            check(refinableWeapon.weaponType !is Staff)
            refinableWeapon.getBasicRefine(actualRefine.statType)
        }
        is StaffRefine -> {
            val refinableWeapon = BASIC_REFINABLE_WEAPONS[weaponName]
            check(refinableWeapon.weaponType is Staff)
            refinableWeapon + actualRefine.skillEffect
        }
    }
    return if (colorChange.isNotEmpty()) {
        val weaponColor = when (colorChange.trim()) {
            "R" -> Color.RED
            "G" -> Color.GREEN
            "B" -> Color.BLUE
            else -> throw IllegalArgumentException("wrong weapon name $weaponName, color can only be one of R,G,B")
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
    spawnMap: Map<Int, Spawn>
) : BattleMap {
    override val enemyCount = spawnMap.size
    private val spawnMap = spawnMap.toMap()
    override val size: Position = positionMap.size
    override val terrainMap = positionMap.terrainMap.toMap()
    private val obstacles = positionMap.obstacles.toMap()
    private val idMap = positionMap.idMap.toMap()
    private val playerIds = positionMap.playerIds.toList()
    override val playerCount: Int = playerIds.size

    init {
        val invalidId = spawnMap.keys.asSequence().filterNot {
            positionMap.idMap.containsKey(it)
        }.firstOrNull()
        require(invalidId == null) { "invalid id $invalidId" }
    }

    override fun toChessPieceMap(playerUnits: List<HeroUnit>): Map<Position, ChessPiece> {
        require(playerUnits.size == playerCount)
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
        } + playerIds.asSequence().zip(playerUnits.asSequence()).map { (id, baseUnit) ->
            val position = idMap[id] ?: throw IllegalStateException()
            baseUnit.copy(id, position)
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