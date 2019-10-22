package com.bloombase.feh

sealed class ChessPiece {
    abstract fun copy(): ChessPiece
}

class HeroUnit(val id: Int, private val heroModel: HeroModel, val team: Team) : ChessPiece(), Hero by heroModel {
    val travelPower: Int
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

    fun getColorAdvantage(foe: HeroUnit): Int {
        val attackerColor = weaponType.color
        val defenderColor = foe.weaponType.color
        return when (attackerColor) {
            Color.RED -> when (defenderColor) {
                Color.BLUE -> -20
                Color.GREEN -> 20
                else -> 0
            }
            Color.GREEN -> when (defenderColor) {
                Color.BLUE -> 20
                Color.RED -> -20
                else -> 0
            }
            Color.BLUE -> when (defenderColor) {
                Color.GREEN -> -20
                Color.RED -> 20
                else -> 0
            }
            Color.COLORLESS -> 0
        }
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

class Obstacle(var health: Int) : ChessPiece() {
    override fun copy(): ChessPiece {
        return Obstacle(health)
    }
}

