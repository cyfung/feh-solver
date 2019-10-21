package com.bloombase.feh

interface Skill {
    val extraStat: Stat?
        get() = null
    val coolDownCountAdj: Int
        get() = 0
    val debuffer: Int
        get() = 0
    val hasSpecialDebuff: Boolean
        get() = false

    val startOfTurn: MapSkillMethod?
        get() = null

    val ignoreRange: CombatSkillMethod<Boolean>?
        get() = null
    val disablePriorityChange: CombatSkillMethod<Boolean>?
        get() = null
    val desperation: CombatSkillMethod<Boolean>?
        get() = null
    val vantage: CombatSkillMethod<Boolean>?
        get() = null
    val followUpSelf: CombatSkillMethod<Int>?
        get() = null
    val followUpOpponent: CombatSkillMethod<Int>?
        get() = null
    val buff: CombatSkillMethod<Stat>?
        get() = null
    val debuff: CombatSkillMethod<Stat>?
        get() = null
    val cooldownBuff: CombatSkillMethod<Int>?
        get() = null
    val cooldownDebuff: CombatSkillMethod<Int>?
        get() = null
    val postCombat: CombatSkillMethod<Unit>?
        get() = null
    val supportInCombatBuff: CombatSkillMethod<Skill>?
        get() = null
    val supportInCombatDebuff: CombatSkillMethod<Skill>?
        get() = null
}


class SkillSet(skills: List<Skill>) {
    val skills = skills.toList()
    val startOfTurn = this.skills.mapNotNull(Skill::startOfTurn)
    val ignoreRange = this.skills.mapNotNull(Skill::ignoreRange)
    val disablePriorityChange = this.skills.mapNotNull(Skill::disablePriorityChange)
    val desperation = this.skills.mapNotNull(Skill::desperation)
    val vantage = this.skills.mapNotNull(Skill::vantage)
    val followUpSelf = this.skills.mapNotNull(Skill::followUpSelf)
    val followUpOpponent = this.skills.mapNotNull(Skill::followUpOpponent)
    val buffSkill = this.skills.mapNotNull(Skill::buff)
    val debuffSkill = this.skills.mapNotNull(Skill::debuff)
    val cooldownBuff = this.skills.mapNotNull(Skill::cooldownBuff)
    val cooldownDebuff = this.skills.mapNotNull(Skill::cooldownDebuff)
    val postCombat = this.skills.mapNotNull(Skill::postCombat)
}

abstract class Weapon(val weaponType: WeaponType) : Skill {
}

abstract class BasicWeapon(weaponType: WeaponType, might: Int) : Weapon(weaponType) {
    override val extraStat = Stat(atk = might)
}

abstract class Special(val coolDownCount: Int) : Skill
interface Assists : Skill

interface Passive : Skill

interface CombatSkillMethod<T> {
    fun apply(battleState: BattleState, self: HeroUnit, opponent: HeroUnit, attack: Boolean): T
}

interface MapSkillMethod {
    fun apply(battleState: BattleState, self: HeroUnit)
}

open class ConstantCombatSkillMethod<T>(private val value: T) : CombatSkillMethod<T> {
    override fun apply(battleState: BattleState, self: HeroUnit, opponent: HeroUnit, attack: Boolean): T {
        return value
    }
}

object CombatSkillMethodTrue : ConstantCombatSkillMethod<Boolean>(true)