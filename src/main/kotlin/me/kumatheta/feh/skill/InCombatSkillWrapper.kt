package me.kumatheta.feh.skill

import me.kumatheta.feh.*

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
        CombatStatus(
            baseSkillSet.combatStatus.battleState,
            self,
            foe,
            baseSkillSet.combatStatus.initAttack
        )

    val inCombatStat: InCombatStat
        get() = self

    val heroUnit
        get() = self.heroUnit

    private fun <T> Sequence<MapSkillMethod<T>>.mapSkillApplyAll(): Sequence<T> {
        return map { it(combatStatus.battleState, combatStatus.self.heroUnit) }
    }

    private fun <T> Sequence<InCombatSkill<T>>.applyAll(): Sequence<T> {
        return map { it(combatStatus) }
    }

    private fun <T> Sequence<(CombatStatus<InCombatStat>, specialTriggered: Boolean) -> T>.applyAllPerAttack(
        specialTriggered: Boolean
    ): Sequence<T> {
        return map { it(combatStatus, specialTriggered) }
    }

    private fun <T> Sequence<PerAttackListener<T>>.applyAllPerAttack(value: T) {
        return forEach { it(combatStatus, value) }
    }

    private fun Sequence<CooldownChange<Int>>.max(): CooldownChange<Int> {
        return fold(CooldownChange(0, 0)) { acc, cooldownChange ->
            CooldownChange(
                kotlin.math.max(acc.unitAttack, cooldownChange.foeAttack),
                kotlin.math.max(acc.foeAttack, cooldownChange.unitAttack)
            )
        }
    }



    val adaptiveDamage: Boolean
        get() = baseSkillSet.adaptiveDamage
    val denyAdaptiveDamage: Boolean
        get() = baseSkillSet.denyAdaptiveDamage

    val canCounter: Boolean = baseSkillSet.counter.sum() >= 0
    val followUp: Int
        get() = baseSkillSet.followUp.sum()
    val desperation: Boolean
        get() = baseSkillSet.desperation.any { it }
    val vantage: Boolean
        get() = baseSkillSet.vantage.any { it }

    val additionalInCombatStat: Stat = baseSkillSet.additionalInCombatStat.applyAll().fold(Stat.ZERO) { acc, stat ->
        acc + stat
    }
    val staffAsNormal: Boolean = baseSkillSet.staffAsNormal.applyAll().any{ it }
    val denyStaffAsNormal: Boolean = baseSkillSet.denyStaffAsNormal.applyAll().any { it }

    val raven: Boolean
        get() = baseSkillSet.raven.applyAll().any { it }
    val cancelAffinity: Int
        get() = baseSkillSet.cancelAffinity.applyAll().max() ?: 0
    val triangleAdept: Int
        get() = baseSkillSet.triangleAdept.applyAll().max() ?: 0
    val cooldownBuff: CooldownChange<Int>
        get() = baseSkillSet.cooldownBuff.applyAll().max()
    val cooldownDebuff: CooldownChange<Int>
        get() = baseSkillSet.cooldownDebuff.applyAll().max()
    val counterIgnoreRange: Boolean
        get() = baseSkillSet.counterIgnoreRange.applyAll().any { it }
    val brave: Boolean
        get() = baseSkillSet.brave.applyAll().any { it }
    val disablePriorityChange: Boolean
        get() = baseSkillSet.disablePriorityChange.applyAll().any { it }

    fun getPercentageDamageReduce(specialTriggered: Boolean): Sequence<Int> =
        baseSkillSet.percentageDamageReduce.applyAllPerAttack(specialTriggered).filterNot { it == 0 }

    fun getFlatDamageReduce(specialTriggered: Boolean): Int =
        baseSkillSet.flatDamageReduce.applyAllPerAttack(specialTriggered).sum()

    fun getDamageIncrease(specialTriggered: Boolean): Int =
        baseSkillSet.damageIncrease.applyAllPerAttack(specialTriggered).sum()

    fun damageDealt(damageDealt: DamageDealt) = baseSkillSet.damageDealtListener.applyAllPerAttack(damageDealt)
    fun damageReceived(damageDealt: DamageDealt) = baseSkillSet.damageReceivedListener.applyAllPerAttack(damageDealt)

    fun postCombat(attacked: Boolean) = baseSkillSet.postCombat.forEach {
        it(combatStatus, attacked)
    }

    val neutralizeFollowUp = baseSkillSet.neutralizeFollowUp.any { it }
}