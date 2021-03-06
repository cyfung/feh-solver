package me.kumatheta.feh

sealed class ChessPiece {
    abstract val position: Position
    abstract fun copy(): ChessPiece
}

class HeroUnit(
    val id: Int,
    private val heroModel: HeroModel,
    val team: Team,
    override var position: Position,
    cooldown: Int? = null
) : ChessPiece(), Hero by heroModel {
    val virtualSpd: Int
        get() = visibleStat.spd + phantomStat.spd
    val weapon
        get() = heroModel.weapon
    private var engageCountDown = heroModel.engageDelay
    var engaged = heroModel.group == null && heroModel.engageDelay == null
        private set
    private var engageCoolDownStarted = heroModel.group == null

    val hasPositiveStatus: Boolean
        get() = positiveStatus.isNotEmpty()
    val hasNegativeStatus: Boolean
        get() = negativeStatus.isNotEmpty()

    private var _cachedEffect: CachedEffect? = null
    val cachedEffect
        get() = _cachedEffect ?: error("not the right time to use cached effect")

    private val positiveStatus = mutableSetOf<PositiveStatus>()
    fun addPositiveStatus(status: PositiveStatus) {
        positiveStatus.add(status)
    }

    private val negativeStatus = mutableSetOf<NegativeStatus>()
    val currentStatTotal: Int
        get() {
            return baseStat.totalExceptHp + buff.totalExceptHp + debuff.totalExceptHp
        }
    val travelPower: Int
        get() = when (heroModel.moveType) {
            MoveType.CAVALRY -> 3
            MoveType.INFANTRY -> 2
            MoveType.ARMORED -> 1
            MoveType.FLYING -> 2
        } + if (withPositiveStatus(PositiveStatus.EXTRA_TRAVEL_POWER)) {
            1
        } else {
            0
        }

    var available = true
        private set
    var buff = Stat.ZERO
        private set
    var debuff = Stat.ZERO
        private set

    fun extraBuffAmount(bonus: Stat): Int {
        return maxOf(bonus.atk - buff.atk, 0) +
                maxOf(bonus.spd - buff.spd, 0) +
                maxOf(bonus.def - buff.def, 0) +
                maxOf(bonus.res - buff.res, 0)
    }


    val maxHp
        get() = baseStat.hp

    fun withNegativeStatus(ns: NegativeStatus): Boolean {
        return negativeStatus.contains(ns)
    }

    fun withPositiveStatus(ps: PositiveStatus): Boolean {
        return positiveStatus.contains(ps)
    }

    val withPanic: Boolean
        get() = negativeStatus.contains(NegativeStatus.PANIC)
    val withIsolation: Boolean
        get() = negativeStatus.contains(NegativeStatus.ISOLATION)
    val withMoveOrder: Boolean
        get() = positiveStatus.contains(PositiveStatus.MOVEMENT_ORDER)

    private var visibleStatBeforeBuffDebuff = baseStat
    val visibleStat: Stat
        get() = visibleStatBeforeBuffDebuff + debuff + if (withPanic) {
            -buff
        } else {
            buff
        }
    val bonus: Stat
        get() = if (withPanic) {
            Stat.ZERO
        } else {
            buff
        }
    val penalty: Stat
        get() = if (withPanic) {
            -buff + debuff
        } else {
            debuff
        }

    var currentHp = baseStat.hp
        private set
    var cooldown = cooldown ?: heroModel.cooldownCount
        private set
    val isDead
        get() = currentHp == 0

    val combatSkillData = mutableMapOf<String, Any>()

    override fun copy(): HeroUnit {
        return copy(id = null, position = null)
    }

    fun copy(id: Int?, position: Position?): HeroUnit {
        require(_cachedEffect == null)
        val newUnit = HeroUnit(id ?: this.id, heroModel, team, position ?: this.position)
        newUnit.available = available
        newUnit.buff = buff
        newUnit.debuff = debuff
        newUnit.currentHp = currentHp
        newUnit.cooldown = cooldown
        newUnit.positiveStatus.addAll(positiveStatus)
        newUnit.negativeStatus.addAll(negativeStatus)
        newUnit.engaged = engaged
        newUnit.engageCountDown = engageCountDown
        newUnit.engageCoolDownStarted = engageCoolDownStarted
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

    fun takeNonLethalDamage(damage: Int): Int {
        require(_cachedEffect == null)
        check(damage >= 0)
        if (damage == 0) return 0
        currentHp -= damage
        return if (currentHp < 1) {
            val actualDamage = damage - (1 - currentHp)
            currentHp = 1
            actualDamage
        } else {
            damage
        }
    }

    fun takeDamage(damage: Int): Int {
        check(damage >= 0)
        if (damage == 0) return currentHp
        val hpBefore = currentHp
        currentHp -= damage
        if (currentHp <= 0) {
            currentHp = 0
        }
        return hpBefore
    }

    fun reduceCooldown(count: Int) {
        require(_cachedEffect == null)
        val cooldown = cooldown ?: return
        this.cooldown = when {
            count <= 0 -> return
            else -> maxOf(cooldown - count, 0)
        }
    }

    fun addCooldown(count: Int) {
        require(_cachedEffect == null)
        val cooldown = cooldown ?: return
        val cooldownCount = heroModel.cooldownCount ?: return
        this.cooldown = when {
            count <= 0 -> return
            else -> minOf(cooldownCount, cooldown + count)
        }
    }

    fun applyDebuff(stat: Stat) {
        require(_cachedEffect == null)
        debuff = min(debuff, stat)
    }

    fun applyBuff(stat: Stat) {
        require(_cachedEffect == null)
        buff = max(buff, stat)
    }

    fun clearPenalty() {
        debuff = Stat.ZERO
    }

    fun actionEnded() {
        clearPenalty()
        clearNegativeStatus()
        available = false
    }

    fun refresh() {
        available = true
    }

    fun endOfTurn() {
        available = true
    }

    fun startOfTurn(battleState: BattleState) {
        clearBonus()
        visibleStatBeforeBuffDebuff = transform?.transform(battleState, this) ?: baseStat
        if (!engaged && engageCoolDownStarted) {
            val engageCountDown = engageCountDown
            require(engageCountDown != null)
            val newCountDown = engageCountDown - 1
            this.engageCountDown = newCountDown
            if (newCountDown == 0) {
                engaged = true
            }
        }
    }

    private fun clearBonus() {
        buff = Stat.ZERO
        positiveStatus.clear()
    }

    override fun toString(): String {
        return heroModel.toString() //"HeroUnit(heroModel=$heroModel, team=$team, available=$available, buff=$buff, debuff=$debuff, currentHp=$currentHp, cooldown=$cooldown)"
    }

    fun heal(healAmount: Int): Int {
        require(_cachedEffect == null)
        currentHp += healAmount
        return if (currentHp > maxHp) {
            val actualHeal = healAmount - (currentHp - maxHp)
            currentHp = maxHp
            actualHeal
        } else {
            healAmount
        }
    }

    fun clearNegativeStatus() {
        negativeStatus.clear()
    }

    fun addNegativeStatus(status: NegativeStatus) {
        negativeStatus.add(status)
    }

    fun resetCooldown() {
        cooldown = heroModel.cooldownCount
    }

    fun endOfCombat() {
        if (isDead) return
        applyCachedEffect()
        combatSkillData.clear()
    }

    fun cacheOn() {
        require(_cachedEffect == null)
        _cachedEffect = CachedEffect()
    }

    fun applyCachedEffect() {
        val cachedEffect = _cachedEffect
        this._cachedEffect = null
        requireNotNull(cachedEffect)

        if (!cachedEffect.updated) return
        val hp = cachedEffect.hp
        if (hp > 0) {
            heal(hp)
        } else if (hp < 0) {
            takeNonLethalDamage(-hp)
        }
        val cooldownChange = cachedEffect.cooldown
        if (cooldownChange > 0) {
            addCooldown(cooldownChange)
        } else if (cooldownChange < 0) {
            reduceCooldown(-cooldownChange)
        }
        val cachedBuff = cachedEffect.buff
        if (cachedBuff !== Stat.ZERO) {
            applyBuff(cachedBuff)
        }
        val cachedDebuff = cachedEffect.debuff
        if (cachedDebuff !== Stat.ZERO) {
            applyDebuff(cachedDebuff)
        }
    }

    fun hpThreshold(percentage: Int): Int {
        return compareValues(currentHp * 100, maxHp * percentage)
    }

    fun setEngaged(isSelf: Boolean) {
        when {
            engaged -> return
            isSelf || engageCountDown == null -> engaged = true
            else -> engageCoolDownStarted = true
        }
    }

    fun isEffective(foe: HeroUnit): Boolean {
        return (!foe.neutralizeEffectiveMoveType && effectiveAgainstMoveType.contains(foe.moveType)) ||
                (!foe.neutralizeEffectiveWeaponType && effectiveAgainstWeaponType.contains(foe.weaponType))
    }
}

enum class NegativeStatus {
    GRAVITY,
    PANIC,
    ISOLATION,
    TRIANGLE
}

enum class PositiveStatus {
    MOVEMENT_ORDER,
    EXTRA_TRAVEL_POWER
}

class Obstacle(var health: Int, override val position: Position, val isBreakableByEnemy: Boolean) : ChessPiece() {
    override fun copy(): ChessPiece {
        return Obstacle(health, position, isBreakableByEnemy)
    }
}

