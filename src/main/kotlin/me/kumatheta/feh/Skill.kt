package me.kumatheta.feh

interface Skill {
    val extraStat: Stat?
        get() = null
    val coolDownCountAdj: Int
        get() = 0
    val debuffer: Boolean
        get() = false
    val hasSpecialDebuff: Boolean
        get() = false

    val startOfTurn: MapSkillMethod<Unit>?
        get() = null
    val pass: MapSkillMethod<Boolean>?
        get() = null
    val teleport: MapSkillMethod<Sequence<Position>>?
        get() = null

    val counterIgnoreRange: InCombatSkill<Boolean>?
        get() = null
    val brave: InCombatSkill<Boolean>?
        get() = null
    val disablePriorityChange: InCombatSkill<Boolean>?
        get() = null
    val desperation: InCombatSkill<Boolean>?
        get() = null
    val vantage: InCombatSkill<Boolean>?
        get() = null
    val followUp: InCombatSkill<Int>?
        get() = null
    val cooldownBuff: InCombatSkill<Int>?
        get() = null
    val cooldownDebuff: InCombatSkill<Int>?
        get() = null
    val postCombat: CombatEndSkill?
        get() = null

    val foeEffect: PreCombatSkill<Skill>?
        get() = null
    val inCombatStat: PreCombatSkill<Stat>?
        get() = null

    val supportInCombatBuff: InCombatSkill<Skill>?
        get() = null
    val supportInCombatDebuff: InCombatSkill<Skill>?
        get() = null
}


class SkillSet(skills: Sequence<Skill>) {
    constructor(skills: List<Skill>) : this(skills.asSequence())

    val skills = skills.toList()

    val startOfTurn = this.skills.mapNotNull(Skill::startOfTurn)
    val pass = this.skills.mapNotNull(Skill::pass)
    val teleport = this.skills.mapNotNull(Skill::teleport)

    val counterIgnoreRange = this.skills.mapNotNull(Skill::counterIgnoreRange)
    val brave = this.skills.mapNotNull(Skill::brave)
    val disablePriorityChange = this.skills.mapNotNull(Skill::disablePriorityChange)
    val desperation = this.skills.mapNotNull(Skill::desperation)
    val vantage = this.skills.mapNotNull(Skill::vantage)
    val followUp = this.skills.mapNotNull(Skill::followUp)
    val cooldownBuff = this.skills.mapNotNull(Skill::cooldownBuff)
    val cooldownDebuff = this.skills.mapNotNull(Skill::cooldownDebuff)
    val postCombat = this.skills.mapNotNull(Skill::postCombat)

    val foeEffect = this.skills.mapNotNull(Skill::foeEffect)
    val inCombatStat = this.skills.mapNotNull(Skill::inCombatStat)
}

abstract class Weapon(val weaponType: WeaponType) : Skill {
    open fun isEffective(foe: HeroUnit): Boolean {
        return false
    }
}

abstract class BasicWeapon(weaponType: WeaponType, might: Int) : Weapon(weaponType) {
    override val extraStat = Stat(atk = might)
}

abstract class Special(val coolDownCount: Int) : Skill

abstract class HealingSpecial(coolDownCount: Int) : Special(coolDownCount) {
    abstract fun trigger(self: HeroUnit, target: HeroUnit, battleState: BattleState)
}

interface Passive : Skill

class InCombatStatus(val heroUnit: HeroUnit, val inCombatStat: Stat, val skills: SkillSet)

interface CombatSkill<T, U> {
    fun apply(battleState: BattleState, self: U, foe: U, attack: Boolean): T
}

interface PreCombatSkill<T> : CombatSkill<T, HeroUnit>

interface InCombatSkill<T>: CombatSkill<T, InCombatStatus>

interface CombatEndSkill {
    fun apply(battleState: BattleState, self: HeroUnit, foe: HeroUnit, attack: Boolean, attacked: Boolean)
}

interface MapSkillMethod<T> {
    fun apply(battleState: BattleState, self: HeroUnit): T
}

abstract class ConstantCombatSkill<T>(private val value: T) : InCombatSkill<T> {
    final override fun apply(battleState: BattleState, self: InCombatStatus, foe: InCombatStatus, attack: Boolean): T {
        return value
    }
}

object CombatSkillTrue : ConstantCombatSkill<Boolean>(true)