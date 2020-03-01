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