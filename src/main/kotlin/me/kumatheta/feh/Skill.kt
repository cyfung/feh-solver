package me.kumatheta.feh

import me.kumatheta.feh.skill.Assist
import me.kumatheta.feh.skill.MovementEffect

interface Skill {
    val postInitiateMovement: MovementEffect?
        get() = null
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

    // outside of combat
    val startOfTurn: MapSkillMethod<Unit>?
        get() = null
    val pass: MapSkillMethod<Boolean>?
        get() = null
    val adaptiveDamage: MapSkillMethod<Boolean>?
        get() = null
    val denyAdaptiveDamage: MapSkillMethod<Boolean>?
        get() = null
    val teleport: MapSkillMethod<Sequence<Position>>?
        get() = null
    val supportInCombatBuff: MapSkillWithTarget<Skill?>?
        get() = null
    val supportInCombatDebuff: MapSkillWithTarget<Skill?>?
        get() = null
    val onHealOthers: ((battleState: BattleState, self: HeroUnit, target: HeroUnit, healAmount: Int) -> Unit)?
        get() = null

    val assistRelated: AssistRelated?
        get() = null


    // very beginning of combat
    val foeEffect: CombatStartSkill<Skill?>?
        get() = null
    val neutralizeFollowUp: CombatStartSkill<Boolean>?
        get() = null
    val neutralizeBonus: CombatStartSkill<Stat?>?
        get() = null
    val neutralizePenalty: CombatStartSkill<Stat?>?
        get() = null
    val inCombatStat: CombatStartSkill<Stat>?
        get() = null
    val additionalInCombatStat: InCombatSkill<Stat>?
        get() = null
    val counter: CombatStartSkill<Int>?
        get() = null
    val followUp: CombatStartSkill<Int>?
        get() = null
    val desperation: CombatStartSkill<Boolean>?
        get() = null
    val vantage: CombatStartSkill<Boolean>?
        get() = null

    // actual in combat
    val counterIgnoreRange: InCombatSkill<Boolean>?
        get() = null
    val brave: InCombatSkill<Boolean>?
        get() = null
    val disablePriorityChange: InCombatSkill<Boolean>?
        get() = null
    val cooldownBuff: InCombatSkill<CooldownChange<Int>>?
        get() = null
    val cooldownDebuff: InCombatSkill<CooldownChange<Int>>?
        get() = null
    val triangleAdept: InCombatSkill<Int>?
        get() = null
    val cancelAffinity: InCombatSkill<Int>?
        get() = null
    val raven: InCombatSkill<Boolean>?
        get() = null
    val staffAsNormal: InCombatSkill<Boolean>?
        get() = null
    val denyStaffAsNormal: InCombatSkill<Boolean>?
        get() = null


    // per attack skill
    val percentageDamageReduce: PerAttackSkill<Int>?
        get() = null
    val flatDamageReduce: PerAttackSkill<Int>?
        get() = null
    val damageIncrease: PerAttackSkill<Int>?
        get() = null

    // listener
    val damageDealtListener: PerAttackListener<DamageDealt>?
        get() = null
    val damageReceivedListener: PerAttackListener<DamageDealt>?
        get() = null

    // combat end
    val combatEnd: CombatEndSkill?
        get() = null
}

class CooldownChange<T>(val unitAttack: T, val foeAttack: T)

class SkillMethodSeq<T, U, S : CombatSkill<T, U>>(private val owner: U, private val sequence: Sequence<S>) {

}

class SkillSet(skills: Sequence<Skill>) {
    constructor(skills: List<Skill>) : this(skills.asSequence())

    val skills = skills.toList()

    val postInitiateMovement: MovementEffect?

    init {
        val movements = this.skills.mapNotNull(Skill::postInitiateMovement)
        if (movements.size > 1) {
            throw IllegalStateException("more than one possible movement post combat")
        }
        postInitiateMovement = movements.singleOrNull()
    }

    val startOfTurn = this.skills.mapNotNull(Skill::startOfTurn)
    val pass = this.skills.mapNotNull(Skill::pass)
    val teleport = this.skills.mapNotNull(Skill::teleport)

    val foeEffect = this.skills.mapNotNull(Skill::foeEffect)

    val supportInCombatBuff = this.skills.mapNotNull(Skill::supportInCombatBuff)
    val supportInCombatDebuff = this.skills.mapNotNull(Skill::supportInCombatDebuff)
    val onHealOthers = this.skills.mapNotNull(Skill::onHealOthers)

    val assistRelated = this.skills.mapNotNull(Skill::assistRelated)

    fun <T> groupAsSet(f: (Skill) -> Set<T>?): Set<T> {
        return skills.asSequence().mapNotNull(f).flatMap { it.asSequence() }.toSet()
    }
}


class InCombatSkillSet(
    battleState: BattleState,
    self: HeroUnit,
    foe: HeroUnit,
    initAttack: Boolean,
    skills: Sequence<Skill>
) {
    val combatStatus = CombatStatus(battleState, self, foe, initAttack)
    private val skills = skills.toList()

    private fun <T : Any> methodSeq(transform: (Skill) -> T?): Sequence<T> {
        return skills.asSequence().mapNotNull(transform)
    }

    private fun <T> Sequence<CombatStartSkill<T>>.applyAll(): Sequence<T> {
        return map { it.invoke(combatStatus) }
    }

    val adaptiveDamage: Sequence<MapSkillMethod<Boolean>>
        get() = methodSeq(Skill::adaptiveDamage)
    val denyAdaptiveDamage: Sequence<MapSkillMethod<Boolean>>
        get() = methodSeq(Skill::denyAdaptiveDamage)


    val neutralizeFollowUp: Sequence<CombatStartSkill<Boolean>>
        get() = methodSeq(Skill::neutralizeFollowUp)
    val neutralizeBonus: Sequence<Stat?>
        get() = methodSeq(Skill::neutralizeBonus).applyAll()
    val neutralizePenalty: Sequence<Stat?>
        get() = methodSeq(Skill::neutralizePenalty).applyAll()
    val inCombatStat: Sequence<Stat>
        get() = methodSeq(Skill::inCombatStat).applyAll()
    val counter: Sequence<Int>
        get() = methodSeq(Skill::counter).applyAll()
    val followUp: Sequence<Int>
        get() = methodSeq(Skill::followUp).applyAll()
    val desperation: Sequence<Boolean>
        get() = methodSeq(Skill::desperation).applyAll()
    val vantage: Sequence<Boolean>
        get() = methodSeq(Skill::vantage).applyAll()


    val additionalInCombatStat: Sequence<InCombatSkill<Stat>>
        get() = methodSeq(Skill::additionalInCombatStat)
    val staffAsNormal: Sequence<InCombatSkill<Boolean>>
        get() = methodSeq(Skill::staffAsNormal)
    val denyStaffAsNormal: Sequence<InCombatSkill<Boolean>>
        get() = methodSeq(Skill::denyStaffAsNormal)
    val raven: Sequence<InCombatSkill<Boolean>>
        get() = methodSeq(Skill::raven)
    val counterIgnoreRange: Sequence<InCombatSkill<Boolean>>
        get() = methodSeq(Skill::counterIgnoreRange)
    val brave: Sequence<InCombatSkill<Boolean>>
        get() = methodSeq(Skill::brave)
    val disablePriorityChange: Sequence<InCombatSkill<Boolean>>
        get() = methodSeq(Skill::disablePriorityChange)
    val cooldownBuff: Sequence<InCombatSkill<CooldownChange<Int>>>
        get() = methodSeq(Skill::cooldownBuff)
    val cooldownDebuff: Sequence<InCombatSkill<CooldownChange<Int>>>
        get() = methodSeq(Skill::cooldownDebuff)
    val triangleAdept: Sequence<InCombatSkill<Int>>
        get() = methodSeq(Skill::triangleAdept)
    val cancelAffinity: Sequence<InCombatSkill<Int>>
        get() = methodSeq(Skill::cancelAffinity)


    // per attack
    val percentageDamageReduce: Sequence<PerAttackSkill<Int>>
        get() = methodSeq(Skill::percentageDamageReduce)
    val flatDamageReduce: Sequence<PerAttackSkill<Int>>
        get() = methodSeq(Skill::flatDamageReduce)
    val damageIncrease: Sequence<PerAttackSkill<Int>>
        get() = methodSeq(Skill::damageIncrease)

    // listener
    val damageDealtListener: Sequence<PerAttackListener<DamageDealt>>
        get() = methodSeq(Skill::damageDealtListener)
    val damageReceivedListener: Sequence<PerAttackListener<DamageDealt>>
        get() = methodSeq(Skill::damageReceivedListener)


    val postCombat: Sequence<CombatEndSkill>
        get() = methodSeq(Skill::combatEnd)
}

class InCombatSkillWrapper(
    private val self: InCombatStat,
    private val foe: InCombatStat,
    private val baseSkillSet: InCombatSkillSet
) {
    init {
        check(baseSkillSet.combatStatus.self == self.heroUnit)
        check(baseSkillSet.combatStatus.foe == foe.heroUnit)
    }

    private val combatStatus =
        CombatStatus(baseSkillSet.combatStatus.battleState, self, foe, baseSkillSet.combatStatus.initAttack)

    val inCombatStat: InCombatStat
        get() = self

    private fun <T> Sequence<MapSkillMethod<T>>.mapSkillApplyAll(): Sequence<T> {
        return map { it(combatStatus.battleState, combatStatus.self.heroUnit) }
    }

    private fun <T> Sequence<InCombatSkill<T>>.applyAll(): Sequence<T> {
        return map { it(combatStatus) }
    }

    private fun <T> Sequence<PerAttackSkill<T>>.applyAllPerAttack(specialTriggered: Boolean): Sequence<T> {
        return map { it(combatStatus, specialTriggered) }
    }

    private fun <T> Sequence<PerAttackListener<T>>.applyAllPerAttack(value: T) {
        return forEach { it(combatStatus, value) }
    }

    val adaptiveDamage: Sequence<Boolean>
        get() = baseSkillSet.adaptiveDamage.mapSkillApplyAll()
    val denyAdaptiveDamage: Sequence<Boolean>
        get() = baseSkillSet.denyAdaptiveDamage.mapSkillApplyAll()

    val counter: Sequence<Int>
        get() = baseSkillSet.counter
    val followUp: Sequence<Int>
        get() = baseSkillSet.followUp
    val desperation: Sequence<Boolean>
        get() = baseSkillSet.desperation
    val vantage: Sequence<Boolean>
        get() = baseSkillSet.vantage

    val additionalInCombatStat: Sequence<Stat>
        get() = baseSkillSet.additionalInCombatStat.applyAll()
    val staffAsNormal: Sequence<Boolean>
        get() = baseSkillSet.staffAsNormal.applyAll()
    val denyStaffAsNormal: Sequence<Boolean>
        get() = baseSkillSet.denyStaffAsNormal.applyAll()
    val raven: Sequence<Boolean>
        get() = baseSkillSet.raven.applyAll()
    val cancelAffinity: Sequence<Int>
        get() = baseSkillSet.cancelAffinity.applyAll()
    val triangleAdept: Sequence<Int>
        get() = baseSkillSet.triangleAdept.applyAll()
    val cooldownBuff: Sequence<CooldownChange<Int>>
        get() = baseSkillSet.cooldownBuff.applyAll()
    val cooldownDebuff: Sequence<CooldownChange<Int>>
        get() = baseSkillSet.cooldownDebuff.applyAll()
    val counterIgnoreRange: Sequence<Boolean>
        get() = baseSkillSet.counterIgnoreRange.applyAll()
    val brave: Sequence<Boolean>
        get() = baseSkillSet.brave.applyAll()
    val disablePriorityChange: Sequence<Boolean>
        get() = baseSkillSet.disablePriorityChange.applyAll()

    fun getPercentageDamageReduce(specialTriggered: Boolean): Sequence<Int> =
        baseSkillSet.percentageDamageReduce.applyAllPerAttack(specialTriggered)

    fun getFlatDamageReduce(specialTriggered: Boolean): Sequence<Int> =
        baseSkillSet.flatDamageReduce.applyAllPerAttack(specialTriggered)

    fun getDamageIncrease(specialTriggered: Boolean): Sequence<Int> =
        baseSkillSet.damageIncrease.applyAllPerAttack(specialTriggered)

    fun damageDealt(damageDealt: DamageDealt) = baseSkillSet.damageDealtListener.applyAllPerAttack(damageDealt)
    fun damageReceived(damageDealt: DamageDealt) = baseSkillSet.damageReceivedListener.applyAllPerAttack(damageDealt)

    fun postCombat(attacked: Boolean) = baseSkillSet.postCombat.forEach {
        it(combatStatus, attacked)
    }

    val neutralizeFollowUp = baseSkillSet.neutralizeFollowUp.any { it(baseSkillSet.combatStatus) }
}

abstract class Weapon(val weaponType: WeaponType) : Skill

open class BasicWeapon(weaponType: WeaponType, might: Int, extraStat: Stat = Stat.ZERO) : Weapon(weaponType) {
    final override val extraStat = Stat(atk = might) + extraStat
}

abstract class Special(val coolDownCount: Int) : Skill

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

interface Passive : Skill

interface InCombatStat {
    val heroUnit: HeroUnit
    val bonus: Stat
    val penalty: Stat
    val inCombatStat: Stat
}

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

typealias PerAttackSkill<T> = (CombatStatus<InCombatStat>, specialTriggered: Boolean) -> T

typealias CombatEndSkill = (CombatStatus<InCombatStat>, attacked: Boolean) -> Unit

typealias PerAttackListener<T> = (CombatStatus<InCombatStat>, value: T) -> Unit

typealias MapSkillMethod<T> = (battleState: BattleState, self: HeroUnit) -> T

interface MapSkillWithTarget<T> {
    fun apply(battleState: BattleState, self: HeroUnit, target: HeroUnit): T
}

interface AssistRelated {
    fun apply(battleState: BattleState, self: HeroUnit, ally: HeroUnit, assist: Assist, useAssist: Boolean)
}

fun <T, U> combatSkill(value: T): CombatSkill<T, U> {
    return {
        value
    }
}

fun <T> inCombatSkill(value: T): InCombatSkill<T> = combatSkill(value)
fun <T> combatStartSkill(value: T): CombatStartSkill<T> = combatSkill(value)

val combatStartSkillTrue: CombatStartSkill<Boolean> = combatSkill(true)

val inCombatSkillTrue: InCombatSkill<Boolean> = combatSkill(true)

data class DamageDealt(
    val attackSpecialTriggered: Boolean,
    val defendSpecialTriggered: Boolean,
    val damage: Int,
    val damageReduced: Int
)