package me.kumatheta.feh.skill

import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.effect.BooleanAdjustment
import me.kumatheta.feh.skill.effect.CancelAffinity

class InCombatSkillWrapper(
    private val self: InCombatStat,
    foe: InCombatStat,
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

    val adaptiveDamage: Boolean
        get() = baseSkillSet.adaptiveDamage
    val denyAdaptiveDamage: Boolean
        get() = baseSkillSet.denyAdaptiveDamage

    val canCounter: Boolean = baseSkillSet.canCounter
    val followUp: BooleanAdjustment
        get() {
            val baseFollowUp = baseSkillSet.followUp
            val neutralizeFollowUp = baseSkillSet.neutralizeFollowUp
            val adjustedFollowUp = if(neutralizeFollowUp.isEmpty()) {
                baseFollowUp
            } else {
                baseFollowUp.filterNot { neutralizeFollowUp.contains(it) }
            }
            val guarantee = adjustedFollowUp.map { it.value }.sum()
            return when {
                guarantee == 0 -> BooleanAdjustment.NEUTRAL
                guarantee > 0 -> BooleanAdjustment.POSITIVE
                else -> BooleanAdjustment.NEGATIVE
            }
        }

    val desperation: Boolean
        get() = baseSkillSet.desperation
    val vantage: Boolean
        get() = baseSkillSet.vantage

    val additionalInCombatStat: Stat =
        baseSkillSet.additionalInCombatStat.map { it.apply(combatStatus) }.fold(Stat.ZERO) { acc, stat ->
            acc + stat
        }
    val staffAsNormal: Boolean = baseSkillSet.staffAsNormal
    val denyStaffAsNormal: Boolean = baseSkillSet.denyStaffAsNormal

    val raven: Boolean
        get() = baseSkillSet.raven
    val cancelAffinity: CancelAffinity.Type?
        get() = baseSkillSet.cancelAffinity
    val triangleAdept: Int
        get() = baseSkillSet.triangleAdept
    val counterAnyRange: Boolean
        get() = baseSkillSet.counterAnyRange
    val brave: Boolean
        get() = baseSkillSet.brave
    val disablePriorityChange: Boolean
        get() = baseSkillSet.disablePriorityChange

    val coolDownChargeEffect
        get() = baseSkillSet.coolDownChargeEffect.map { it.getAdjustment(combatStatus) }

    fun getPercentageDamageReduce(specialTriggered: Boolean): Sequence<Int> =
        baseSkillSet.percentageDamageReduce.map { it.getPercentageDamageReduce(combatStatus, specialTriggered) }
            .filterNot { it == 0 }

    fun getFlatDamageReduce(specialTriggered: Boolean): Int =
        baseSkillSet.flatDamageReduce.map { it.getDamageReduce(combatStatus, specialTriggered) }.sum()

    fun getDamageIncrease(specialTriggered: Boolean): Int =
        baseSkillSet.damageIncrease.map { it.getDamageIncrease(combatStatus, specialTriggered) }.sum()

    fun damageDealt(damageDealt: DamageDealt) =
        baseSkillSet.damageDealtListener.forEach { it.onDamageDealt(combatStatus, damageDealt) }

    fun damageReceived(damageDealt: DamageDealt) =
        baseSkillSet.damageReceivedListener.forEach { it.onDamageReceived(combatStatus, damageDealt) }

    fun postCombat(attacked: Boolean) = baseSkillSet.postCombat.forEach {
        it.onCombatEnd(combatStatus, attacked)
    }

}