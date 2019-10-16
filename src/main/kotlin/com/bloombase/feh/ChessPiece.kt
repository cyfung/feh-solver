package com.bloombase.feh

sealed class ChessPiece(val id: Int) {
    abstract fun copy(): ChessPiece

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ChessPiece) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id
    }
}

enum class Team {
    PLAYER,
    ENEMY;
}

val Team.opponent: Team
    get() {
        return when (this) {
            Team.PLAYER -> Team.ENEMY
            Team.ENEMY -> Team.PLAYER
        }
    }


class HeroUnit(id: Int, private val heroModel: HeroModel, val team: Team) : ChessPiece(id), Hero by heroModel {

    val travelDistance: Int
        get() = when (heroModel.moveType) {
            MoveType.CAVALRY -> 3
            MoveType.INFANTRY -> 2
            MoveType.ARMORED -> 1
            MoveType.FLYING -> 2
        }

    var available = false
    var buff = Stat.ZERO
        private set
    var debuff = Stat.ZERO
        private set
    var currentHp = stat.hp
        private set
    private var cooldown = cooldownCount
    val isDead
        get() = currentHp == 0

    override fun copy(): HeroUnit {
        val newUnit = HeroUnit(id, heroModel, team)
        newUnit.available = available
        newUnit.buff = buff
        newUnit.debuff = debuff
        newUnit.currentHp = currentHp
        newUnit.cooldown = cooldown
        return newUnit
    }

    fun takeDamage(damage: Int): Boolean {
        currentHp -= damage
        if (currentHp <= 0) {
            currentHp = 0
            return true
        }
        return false
    }

    fun reduceCooldown(count: Int) {
        val cooldown = cooldown ?: return
        this.cooldown = when {
            count <= 0 -> return
            count > cooldown -> 0
            else -> cooldown - count
        }
    }

    fun applyDebuff(stat: Stat) {
        debuff = min(debuff, stat)
    }

    fun applyBuff(stat: Stat) {
        buff = max(buff, stat)
    }

    private fun clearPenalty() {
        debuff = Stat.ZERO
    }

    fun endOfTurn() {
        clearPenalty()
        available = false
    }

    fun startOfTurn() {
        clearBonus()
        available = true
    }

    private fun clearBonus() {
        buff = Stat.ZERO
    }

    override fun toString(): String {
        return heroModel.toString() //"HeroUnit(heroModel=$heroModel, team=$team, available=$available, buff=$buff, debuff=$debuff, currentHp=$currentHp, cooldown=$cooldown)"
    }
}

class StationaryObject(id: Int, terrain: Terrain) : ChessPiece(id) {
    override fun copy(): ChessPiece {
        return this
    }

    init {
        when (terrain) {
            Terrain.WALL,
            Terrain.OBSTACLE1,
            Terrain.OBSTACLE2 -> Unit
            else -> throw IllegalArgumentException()
        }
    }
}

enum class Terrain {
    WALL,
    OBSTACLE1,
    OBSTACLE2,
    TREE
}