package me.kumatheta.feh.skill

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat
import me.kumatheta.feh.WeaponType
import me.kumatheta.feh.skill.effect.SkillEffect

interface Skill {
    val effects: List<SkillEffect>
}

val EmptySkill = object : Skill {
    override val effects: List<SkillEffect> = emptyList()
}

class CooldownChange(val unitAttack: Int, val foeAttack: Int)

val NoCooldownChange = CooldownChange(0, 0)

interface Weapon : Skill {
    val weaponType: WeaponType
}

data class BasicWeapon(override val weaponType: WeaponType, val skill: Skill) :
    Weapon, Skill by skill

abstract class Special private constructor(val coolDownCount: Int, skill: Skill) : Skill by skill {
    constructor(coolDownCount: Int, skillEffects: Array<out SkillEffect>) : this(
        coolDownCount,
        skillEffects.asSequence().toSkill()
    )
}

abstract class AoeSpecial(coolDownCount: Int, vararg skillEffects: SkillEffect) : Special(coolDownCount, skillEffects) {
    abstract val damageFactor: Int

    abstract fun getTargets(battleState: BattleState, self: HeroUnit, mainTarget: HeroUnit): Sequence<HeroUnit>
}

abstract class HealingSpecial(coolDownCount: Int, vararg skillEffects: SkillEffect) :
    Special(coolDownCount, skillEffects) {
    abstract fun trigger(self: HeroUnit, target: HeroUnit, battleState: BattleState)
}

abstract class DamagingSpecial(coolDownCount: Int, vararg skillEffects: SkillEffect) :
    Special(coolDownCount, skillEffects) {
    abstract fun getDamage(
        battleState: BattleState,
        self: InCombatStat,
        foe: InCombatStat,
        defenderDefRes: Int,
        atk: Int
    ): Int
}

abstract class DefenseSpecial(coolDownCount: Int, vararg skillEffects: SkillEffect) :
    Special(coolDownCount, skillEffects) {
    abstract fun getReducedDamage(
        battleState: BattleState,
        self: InCombatStat,
        foe: InCombatStat,
        incomingDamage: Int
    ): Int?
}

abstract class PostCombatSpecial(coolDownCount: Int, vararg skillEffects: SkillEffect) :
    Special(coolDownCount, skillEffects) {

    abstract fun postCombat(
        battleState: BattleState,
        self: HeroUnit
    )
}

typealias Passive = Skill

interface InCombatStat {
    val heroUnit: HeroUnit
    val bonus: Stat
    val penalty: Stat
    val inCombatStat: Stat
}

val InCombatStat.inCombatVirtualSpd
    get() = inCombatStat.spd + heroUnit.phantomStat.spd

class BasicInCombatStat(
    override val heroUnit: HeroUnit,
    override val bonus: Stat,
    override val penalty: Stat,
    override var inCombatStat: Stat
) : InCombatStat

class FullInCombatStat(
    val skills: InCombatSkillWrapper,
    val adaptiveDamage: Boolean,
    val reducedStaffDamage: Boolean
) : InCombatStat by skills.inCombatStat

data class CombatStatus<T>(
    val battleState: BattleState,
    val self: T,
    val foe: T,
    val initAttack: Boolean
)

data class DamageDealt(
    val attackSpecialTriggered: Boolean,
    val defendSpecialTriggered: Boolean,
    val damage: Int,
    val damageReduced: Int
)