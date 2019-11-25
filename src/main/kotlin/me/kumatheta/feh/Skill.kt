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
    val cooldownBuff: InCombatSkill<CooldownChange<Int>>?
        get() = null
    val cooldownDebuff: InCombatSkill<CooldownChange<Int>>?
        get() = null
    val combatEnd: CombatEndSkill?
        get() = null

    val foeEffect: CombatStartSkill<Skill?>?
        get() = null
    val inCombatStat: CombatStartSkill<Stat>?
        get() = null

    val adaptiveDamage: CombatStartSkill<Boolean>?
        get() = null
    val denyAdaptiveDamage: CombatStartSkill<Boolean>?
        get() = null

    val staffAsNormal: CombatStartSkill<Boolean>?
        get() = null
    val denyStaffAsNormal: CombatStartSkill<Boolean>?
        get() = null

    val supportInCombatBuff: InCombatSkill<Skill>?
        get() = null
    val supportInCombatDebuff: InCombatSkill<Skill>?
        get() = null

}

class CooldownChange<T>(val unitAttack: T, val foeAttack: T)

class SkillSet(skills: Sequence<Skill>) {
    constructor(skills: List<Skill>) : this(skills.asSequence())

    val skills = skills.toList()

    val startOfTurn = this.skills.mapNotNull(Skill::startOfTurn)
    val pass = this.skills.mapNotNull(Skill::pass)
    val teleport = this.skills.mapNotNull(Skill::teleport)

    val foeEffect = this.skills.mapNotNull(Skill::foeEffect)

}

class InCombatSkillSet(skills: Sequence<Skill>) {
    private val skills = skills.toList()

    val inCombatStat: Sequence<CombatStartSkill<Stat>>
        get() = skills.asSequence().mapNotNull(Skill::inCombatStat)
    val adaptiveDamage: Sequence<CombatStartSkill<Boolean>>
        get() = skills.asSequence().mapNotNull(Skill::adaptiveDamage)
    val denyAdaptiveDamage: Sequence<CombatStartSkill<Boolean>>
        get() = skills.asSequence().mapNotNull(Skill::denyAdaptiveDamage)
    val staffAsNormal: Sequence<CombatStartSkill<Boolean>>
        get() = skills.asSequence().mapNotNull(Skill::staffAsNormal)
    val denyStaffAsNormal: Sequence<CombatStartSkill<Boolean>>
        get() = skills.asSequence().mapNotNull(Skill::denyStaffAsNormal)


    val postCombat: Sequence<CombatEndSkill>
        get() = skills.asSequence().mapNotNull(Skill::combatEnd)

    val counterIgnoreRange: Sequence<InCombatSkill<Boolean>>
        get() = skills.asSequence().mapNotNull(Skill::counterIgnoreRange)
    val brave: Sequence<InCombatSkill<Boolean>>
        get() = skills.asSequence().mapNotNull(Skill::brave)
    val disablePriorityChange: Sequence<InCombatSkill<Boolean>>
        get() = skills.asSequence().mapNotNull(Skill::disablePriorityChange)
    val desperation: Sequence<InCombatSkill<Boolean>>
        get() = skills.asSequence().mapNotNull(Skill::desperation)
    val vantage: Sequence<InCombatSkill<Boolean>>
        get() = skills.asSequence().mapNotNull(Skill::vantage)
    val followUp: Sequence<InCombatSkill<Int>>
        get() = skills.asSequence().mapNotNull(Skill::followUp)
    val cooldownBuff: Sequence<InCombatSkill<CooldownChange<Int>>>
        get() = skills.asSequence().mapNotNull(Skill::cooldownBuff)
    val cooldownDebuff: Sequence<InCombatSkill<CooldownChange<Int>>>
        get() = skills.asSequence().mapNotNull(Skill::cooldownDebuff)
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

abstract class DamagingSpecial(coolDownCount: Int) : Special(coolDownCount) {
    abstract fun getDamage(battleState: BattleState, self: InCombatStatus, foe: InCombatStatus): Int
}

interface Passive : Skill

class InCombatStatus(
    val heroUnit: HeroUnit,
    val inCombatStat: Stat,
    val skills: InCombatSkillSet,
    val adaptiveDamage: Boolean,
    val reducedStaffDamage: Boolean
)

interface CombatSkill<T, U> {
    fun apply(battleState: BattleState, self: U, foe: U, initAttack: Boolean): T
}

interface CombatStartSkill<T> : CombatSkill<T, HeroUnit>

interface InCombatSkill<T>: CombatSkill<T, InCombatStatus>

interface CombatEndSkill {
    fun apply(battleState: BattleState, self: HeroUnit, foe: HeroUnit, attack: Boolean, attacked: Boolean)
}

interface MapSkillMethod<T> {
    fun apply(battleState: BattleState, self: HeroUnit): T
}


class ConstantCombatStartSkill<T>(private val value: T) : CombatStartSkill<T> {
    override fun apply(battleState: BattleState, self: HeroUnit, foe: HeroUnit, initAttack: Boolean): T {
        return value
    }
}

class ConstantInCombatSkill<T>(private val value: T) : InCombatSkill<T> {
    override fun apply(battleState: BattleState, self: InCombatStatus, foe: InCombatStatus, initAttack: Boolean): T {
        return value
    }
}

val combatStartSkillTrue = ConstantCombatStartSkill(true)

val inCombatSkillTrue = ConstantInCombatSkill(true)