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
    val neutralizeEffectiveWeaponType: Set<WeaponType>?
        get() = null
    val neutralizeEffectiveMoveType: Set<MoveType>?
        get() = null
    val effectiveAgainstWeaponType: Set<WeaponType>?
        get() = null
    val effectiveAgainstMoveType: Set<MoveType>?
        get() = null

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
    val triangleAdept: InCombatSkill<Int>?
        get() = null
    val neutralizeBonus: CombatStartSkill<Stat?>?
        get() = null
    val neutralizePenalty: CombatStartSkill<Stat?>?
        get() = null

    val foeEffect: CombatStartSkill<Skill?>?
        get() = null
    val inCombatStat: CombatStartSkill<Stat>?
        get() = null
    val additionalInCombatStat: InCombatSkill<Stat>?
        get() = null

    val raven: InCombatSkill<Boolean>?
        get() = null
    val adaptiveDamage: InCombatSkill<Boolean>?
        get() = null
    val denyAdaptiveDamage: InCombatSkill<Boolean>?
        get() = null
    val staffAsNormal: InCombatSkill<Boolean>?
        get() = null
    val denyStaffAsNormal: InCombatSkill<Boolean>?
        get() = null

    val assistRelated: AssistRelated?
        get() = null

    val supportInCombatBuff: MapSkillWithTarget<Skill?>?
        get() = null
    val supportInCombatDebuff: MapSkillWithTarget<Skill?>?
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

    val supportInCombatBuff = this.skills.mapNotNull(Skill::supportInCombatBuff)
    val supportInCombatDebuff = this.skills.mapNotNull(Skill::supportInCombatDebuff)

    val assistRelated = this.skills.mapNotNull(Skill::assistRelated)

    fun <T> groupAsSet(f: (Skill) -> Set<T>?): Set<T> {
        return skills.asSequence().mapNotNull(f).flatMap { it.asSequence() }.toSet()
    }
}

class InCombatSkillSet(skills: Sequence<Skill>) {
    private val skills = skills.toList()

    val inCombatStat: Sequence<CombatStartSkill<Stat>>
        get() = skills.asSequence().mapNotNull(Skill::inCombatStat)

    val additionalInCombatStat: Sequence<InCombatSkill<Stat>>
        get() = skills.asSequence().mapNotNull(Skill::additionalInCombatStat)
    val adaptiveDamage: Sequence<InCombatSkill<Boolean>>
        get() = skills.asSequence().mapNotNull(Skill::adaptiveDamage)
    val denyAdaptiveDamage: Sequence<InCombatSkill<Boolean>>
        get() = skills.asSequence().mapNotNull(Skill::denyAdaptiveDamage)
    val staffAsNormal: Sequence<InCombatSkill<Boolean>>
        get() = skills.asSequence().mapNotNull(Skill::staffAsNormal)
    val denyStaffAsNormal: Sequence<InCombatSkill<Boolean>>
        get() = skills.asSequence().mapNotNull(Skill::denyStaffAsNormal)
    val raven: Sequence<InCombatSkill<Boolean>>
        get() = skills.asSequence().mapNotNull(Skill::raven)


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
    val triangleAdept: Sequence<InCombatSkill<Int>>
        get() = skills.asSequence().mapNotNull(Skill::triangleAdept)
    val cancelAffinity: Sequence<InCombatSkill<Int>>
        get() = skills.asSequence().mapNotNull(Skill::triangleAdept)
    val neutralizeBonus: Sequence<CombatStartSkill<Stat?>>
        get() = skills.asSequence().mapNotNull(Skill::neutralizeBonus)
    val neutralizePenalty: Sequence<CombatStartSkill<Stat?>>
        get() = skills.asSequence().mapNotNull(Skill::neutralizePenalty)
}

abstract class Weapon(val weaponType: WeaponType) : Skill

abstract class BasicWeapon(weaponType: WeaponType, might: Int) : Weapon(weaponType) {
    override val extraStat = Stat(atk = might)
}

abstract class Special(val coolDownCount: Int) : Skill

abstract class HealingSpecial(coolDownCount: Int) : Special(coolDownCount) {
    abstract fun trigger(self: HeroUnit, target: HeroUnit, battleState: BattleState)
}

abstract class DamagingSpecial(coolDownCount: Int) : Special(coolDownCount) {
    abstract fun getDamage(battleState: BattleState, self: InCombatStat, foe: InCombatStat, defenderDefRes: Int): Int
}

abstract class DefenseSpecial(coolDownCount: Int) : Special(coolDownCount) {
    abstract fun getReducedDamage(battleState: BattleState, self: InCombatStat, foe: InCombatStat, incomingDamage: Int): Int?
}

interface Passive : Skill

interface InCombatStat {
    val heroUnit: HeroUnit
    val inCombatStat: Stat
}

class BasicInCombatStat(
    override val heroUnit: HeroUnit,
    override val inCombatStat: Stat
) : InCombatStat

class FullInCombatStat(
    private val basicStat: BasicInCombatStat,
    val skills: InCombatSkillSet,
    val adaptiveDamage: Boolean,
    val reducedStaffDamage: Boolean
) : InCombatStat by basicStat

interface CombatSkill<T, U> {
    fun apply(battleState: BattleState, self: U, foe: U, initAttack: Boolean): T
}

interface CombatStartSkill<T> : CombatSkill<T, HeroUnit>

interface InCombatSkill<T> : CombatSkill<T, InCombatStat>

interface CombatEndSkill {
    fun apply(battleState: BattleState, self: HeroUnit, foe: HeroUnit, attack: Boolean, attacked: Boolean)
}

interface MapSkillMethod<T> {
    fun apply(battleState: BattleState, self: HeroUnit): T
}

interface MapSkillWithTarget<T> {
    fun apply(battleState: BattleState, self: HeroUnit, target: HeroUnit): T
}

interface AssistRelated {
    fun apply(battleState: BattleState, self: HeroUnit, ally: HeroUnit, assist: Assist, useAssist: Boolean)
}

class ConstantCombatStartSkill<T>(private val value: T) : CombatStartSkill<T> {
    override fun apply(battleState: BattleState, self: HeroUnit, foe: HeroUnit, initAttack: Boolean): T {
        return value
    }
}

class ConstantInCombatSkill<T>(private val value: T) : InCombatSkill<T> {
    override fun apply(battleState: BattleState, self: InCombatStat, foe: InCombatStat, initAttack: Boolean): T {
        return value
    }
}

val combatStartSkillTrue = ConstantCombatStartSkill(true)

val inCombatSkillTrue = ConstantInCombatSkill(true)