package me.kumatheta.feh.skill

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat
import me.kumatheta.feh.StatType
import me.kumatheta.feh.skill.effect.AdaptiveDamageEffect
import me.kumatheta.feh.skill.effect.BraveEffect
import me.kumatheta.feh.skill.effect.CanCounter
import me.kumatheta.feh.skill.effect.CancelAffinity
import me.kumatheta.feh.skill.effect.CombatStartEffect
import me.kumatheta.feh.skill.effect.CoolDownChargeEffect
import me.kumatheta.feh.skill.effect.CounterAnyRange
import me.kumatheta.feh.skill.effect.DamageDealtListener
import me.kumatheta.feh.skill.effect.DamageIncrease
import me.kumatheta.feh.skill.effect.DamageReceivedListener
import me.kumatheta.feh.skill.effect.DenyAdaptiveDamageEffect
import me.kumatheta.feh.skill.effect.DenyStaffAsNormal
import me.kumatheta.feh.skill.effect.DesperationEffect
import me.kumatheta.feh.skill.effect.DisablePriorityChange
import me.kumatheta.feh.skill.effect.ExtraInCombatStatEffect
import me.kumatheta.feh.skill.effect.FlatDamageReduce
import me.kumatheta.feh.skill.effect.FollowUpEffect
import me.kumatheta.feh.skill.effect.InCombatSkillEffect
import me.kumatheta.feh.skill.effect.InCombatStatEffect
import me.kumatheta.feh.skill.effect.NeutralizeBonus
import me.kumatheta.feh.skill.effect.NeutralizeFollowUp
import me.kumatheta.feh.skill.effect.NeutralizePenalty
import me.kumatheta.feh.skill.effect.PercentageDamageReduce
import me.kumatheta.feh.skill.effect.PostCombatEffect
import me.kumatheta.feh.skill.effect.Raven
import me.kumatheta.feh.skill.effect.SkillEffect
import me.kumatheta.feh.skill.effect.StaffAsNormal
import me.kumatheta.feh.skill.effect.TriangleAdept
import me.kumatheta.feh.skill.effect.VantageEffect

class InCombatSkillSet(
    battleState: BattleState,
    self: HeroUnit,
    foe: HeroUnit,
    initAttack: Boolean,
    skillEffects: Sequence<InCombatSkillEffect>
) {
    val combatStatus = CombatStatus(battleState, self, foe, initAttack)
    private val skillEffects = skillEffects.toList()

    private inline fun <reified R : InCombatSkillEffect> get(): Sequence<R> {
        return this.skillEffects.asSequence().filterIsInstance<R>()
    }

    private inline fun <T, reified R : CombatStartEffect<T>> applyAll(): Sequence<T> {

        return this.skillEffects.asSequence().filterIsInstance<R>().applyAll()
    }

    private inline fun <reified R : CombatStartEffect<Boolean>> any(): Boolean {
        return applyAll<Boolean, R>().any { it }
    }


    private fun <T> Sequence<CombatStartEffect<T>>.applyAll(): Sequence<T> {
        return map { it.apply(combatStatus) }
    }

    val adaptiveDamage = any<AdaptiveDamageEffect>()
    val denyAdaptiveDamage = any<DenyAdaptiveDamageEffect>()

    val staffAsNormal
        get() = any<StaffAsNormal>()
    val denyStaffAsNormal
        get() = any<DenyStaffAsNormal>()
    val raven
        get() = any<Raven>()
    val counterAnyRange
        get() = any<CounterAnyRange>()
    val brave
        get() = any<BraveEffect>()
    val disablePriorityChange
        get() = any<DisablePriorityChange>()
    val desperation
        get() = any<DesperationEffect>()
    val vantage
        get() = any<VantageEffect>()

    val neutralizeFollowUp: Boolean
        get() = any<NeutralizeFollowUp>()
    val neutralizeBonus: Set<StatType>
        get() = get<NeutralizeBonus>().applyAll().flatMap { it }.toSet()
    val neutralizePenalty: Set<StatType>
        get() = get<NeutralizePenalty>().applyAll().flatMap { it }.toSet()
    val inCombatStat: Sequence<Stat>
        get() = get<InCombatStatEffect>().applyAll()
    val canCounter: Boolean
        get() = get<CanCounter>().applyAll().map { it.value }.sum() >= 0
    val followUp: Int
        get() = get<FollowUpEffect>().applyAll().map { it.value }.sum()
    val triangleAdept: Int
        get() = get<TriangleAdept>().applyAll().max() ?: 0
    val cancelAffinity: CancelAffinity.Type?
        get() = get<CancelAffinity>().applyAll().filterNotNull().max()

    val additionalInCombatStat: Sequence<ExtraInCombatStatEffect>
        get() = get()
    val coolDownChargeEffect: Sequence<CoolDownChargeEffect>
        get() = get()


    // per attack
    val percentageDamageReduce: Sequence<PercentageDamageReduce>
        get() = get()
    val flatDamageReduce: Sequence<FlatDamageReduce>
        get() = get()
    val damageIncrease: Sequence<DamageIncrease>
        get() = get()
    // listener
    val damageDealtListener: Sequence<DamageDealtListener>
        get() = get()
    val damageReceivedListener: Sequence<DamageReceivedListener>
        get() = get()

    val postCombat: Sequence<PostCombatEffect>
        get() = get()
}