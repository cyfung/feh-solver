package me.kumatheta.feh.skill

import me.kumatheta.feh.*
import me.kumatheta.feh.skill.effect.SpecialDebuff
import me.kumatheta.feh.skill.effect.InCombatSupportInput

interface Skill {
    val postInitiateMovement: MovementEffect?
    val extraStat: Stat?
    val phantomStat: Stat?
    val coolDownCountAdj: Int
    val debuffer: Boolean
    val specialDebuff: SpecialDebuff?
    val neutralizeEffectiveWeaponType: Set<WeaponType>?
    val neutralizeEffectiveMoveType: Set<MoveType>?
    val effectiveAgainstWeaponType: Set<WeaponType>?
    val effectiveAgainstMoveType: Set<MoveType>?

    // outside of combat
    val startOfTurn: MapSkillMethod<Unit>?
    val pass: MapSkillMethod<Boolean>?
    val obstruct: MapSkillMethod<Boolean>?
    val teleport: MapSkillMethod<Sequence<Position>>?
    val guidance: ((battleState: BattleState, self: HeroUnit, target: HeroUnit) -> Boolean)?
    val supportInCombatBuff: SupportCombatEffect?
    val supportInCombatDebuff: SupportCombatEffect?
    val onHealOthers: ((battleState: BattleState, self: HeroUnit, target: HeroUnit, healAmount: Int) -> Unit)?

    val assistRelated: AssistRelated?

    // very beginning of combat
    val adaptiveDamage: CombatStartSkill<Boolean>?
    val denyAdaptiveDamage: CombatStartSkill<Boolean>?
    val foeEffect: CombatStartSkill<Skill?>?
    val neutralizeFollowUp: CombatStartSkill<Boolean>?
    val neutralizeBonus: CombatStartSkill<Stat?>?
    val neutralizePenalty: CombatStartSkill<Stat?>?
    val inCombatStat: CombatStartSkill<Stat>?
    val additionalInCombatStat: InCombatSkill<Stat>?
    val counter: CombatStartSkill<Int>?
    val followUp: CombatStartSkill<Int>?
    val desperation: CombatStartSkill<Boolean>?
    val vantage: CombatStartSkill<Boolean>?

    // actual in combat
    val counterIgnoreRange: InCombatSkill<Boolean>?
    val brave: InCombatSkill<Boolean>?
    val disablePriorityChange: InCombatSkill<Boolean>?
    val cooldownBuff: InCombatSkill<CooldownChange?>?
    val cooldownDebuff: InCombatSkill<CooldownChange?>?
    val triangleAdept: InCombatSkill<Int>?
    val cancelAffinity: InCombatSkill<Int>?
    val raven: InCombatSkill<Boolean>?
    val staffAsNormal: InCombatSkill<Boolean>?
    val denyStaffAsNormal: InCombatSkill<Boolean>?

    // per attack skill
    val percentageDamageReduce: ((CombatStatus<InCombatStat>, specialTriggered: Boolean) -> Int)?
    val flatDamageReduce: ((CombatStatus<InCombatStat>, specialTriggered: Boolean) -> Int)?
    val damageIncrease: ((CombatStatus<InCombatStat>, specialTriggered: Boolean) -> Int)?

    // listener
    val damageDealtListener: PerAttackListener<DamageDealt>?
    val damageReceivedListener: PerAttackListener<DamageDealt>?

    // combat end
    val combatEnd: CombatEndSkill?
}

class CooldownChange(val unitAttack: Int, val foeAttack: Int)

interface Weapon : Skill {
    val weaponType: WeaponType
}

data class BasicWeapon(override val weaponType: WeaponType, val basicSkill: BasicSkill) :
    Weapon, Skill by basicSkill

abstract class Special(val coolDownCount: Int, basicSkill: BasicSkill = EmptySkill) : Skill by basicSkill

abstract class AoeSpecial(coolDownCount: Int) : Special(coolDownCount) {
    abstract val damageFactor: Int

    abstract fun getTargets(battleState: BattleState, self: HeroUnit, mainTarget: HeroUnit): Sequence<HeroUnit>
}

abstract class HealingSpecial(coolDownCount: Int) : Special(coolDownCount) {
    abstract fun trigger(self: HeroUnit, target: HeroUnit, battleState: BattleState)
}

abstract class DamagingSpecial(coolDownCount: Int) : Special(coolDownCount) {
    abstract fun getDamage(
        battleState: BattleState,
        self: InCombatStat,
        foe: InCombatStat,
        defenderDefRes: Int,
        atk: Int
    ): Int
}

abstract class DefenseSpecial(coolDownCount: Int) : Special(coolDownCount) {
    abstract fun getReducedDamage(
        battleState: BattleState,
        self: InCombatStat,
        foe: InCombatStat,
        incomingDamage: Int
    ): Int?
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

typealias CombatSkill<T, U> = (CombatStatus<U>) -> T

typealias CombatStartSkill<T> = CombatSkill<T, HeroUnit>

typealias InCombatSkill<T> = CombatSkill<T, InCombatStat>

typealias CombatEndSkill = (CombatStatus<InCombatStat>, attacked: Boolean) -> Unit

typealias PerAttackListener<T> = (CombatStatus<InCombatStat>, value: T) -> Unit

typealias MapSkillMethod<T> = (battleState: BattleState, self: HeroUnit) -> T
typealias TeleportEffect = MapSkillMethod<Sequence<Position>>

typealias SupportCombatEffect = (InCombatSupportInput) -> Skill?


interface AssistRelated {
    fun apply(battleState: BattleState, self: HeroUnit, ally: HeroUnit, assist: Assist, useAssist: Boolean)
}

fun <T, U> combatSkill(value: T): CombatSkill<T, U> {
    return {
        value
    }
}

fun <T> inCombatSkill(value: T): InCombatSkill<T> =
    combatSkill(value)
fun <T> combatStartSkill(value: T): CombatStartSkill<T> =
    combatSkill(value)

val combatStartSkillTrue: CombatStartSkill<Boolean> =
    combatSkill(true)

val inCombatSkillTrue: InCombatSkill<Boolean> =
    combatSkill(true)

data class DamageDealt(
    val attackSpecialTriggered: Boolean,
    val defendSpecialTriggered: Boolean,
    val damage: Int,
    val damageReduced: Int
)