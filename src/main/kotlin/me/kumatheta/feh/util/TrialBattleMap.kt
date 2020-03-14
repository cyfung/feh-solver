package me.kumatheta.feh.util

import me.kumatheta.feh.*
import me.kumatheta.feh.skill.weapon.EmptyWeapon

class TrialBattleMap(
    attacker: HeroUnit,
    defender: HeroUnit
) : BattleMap {
    init {
        require(attacker.team != defender.team)
        require(attacker.weapon !is EmptyWeapon)
    }

    val attacker = attacker.copy()
    val defender = defender.copy()

    init {
        this.defender.position = if (attacker.weaponType.isRanged) {
            Position(1, 1)
        } else {
            Position(1, 0)
        }
        this.attacker.position = Position(0, 0)
    }

    override val size: Position = Position(1, 1)

    override val terrainMap: Map<Position, Terrain> = sequenceOf(
        Position(0, 0),
        Position(0, 1),
        Position(1, 0),
        Position(1, 1)
    ).associateWith { Terrain(Terrain.Type.REGULAR, false) }

    override fun toChessPieceMap(): Map<Position, ChessPiece> {
        return sequenceOf(attacker, defender).associateBy { it.position }
    }

    override val reinforceByTime: Map<Int, List<HeroUnit>>
        get() = emptyMap()

    override val enemyCount: Int = 1
    override val playerCount: Int = 1
}

fun BattleMap.getAllTrials(): Map<AttackerDefenderPair<Int>, TrialResult> {
    val chessPieceMap = toChessPieceMap()
    val (players, initialEnemies) = chessPieceMap.values.asSequence().filterIsInstance<HeroUnit>()
        .partition { it.team == Team.PLAYER }
    val enemies = reinforceByTime.values.asSequence().flatMap { it.asSequence() } +
            initialEnemies
    return enemies.filter { it.weapon !is EmptyWeapon }.flatMap { enemy ->
        players.asSequence().map { player ->
            TrialBattleMap(enemy, player)
        }
    }.associate {
        val battleState = BattleState(CacheBattleMap(it))
        val attackerHpBefore = it.attacker.currentHp
        val defenderHpBefore = it.defender.currentHp
        battleState.fight(it.attacker, it.defender)
        AttackerDefenderPair(it.attacker.id, it.defender.id) to TrialResult(
            attackerHpBefore, defenderHpBefore,
            it.attacker.currentHp, it.defender.currentHp
        )
    }
}

data class TrialResult(
    val attackerHpBefore: Int,
    val defenderHpBefore: Int,
    val attackerHpAfter: Int,
    val defenderHpAfter: Int
)