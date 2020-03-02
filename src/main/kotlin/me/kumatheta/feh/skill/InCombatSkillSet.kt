package me.kumatheta.feh.skill

import me.kumatheta.feh.*

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

    val adaptiveDamage: Boolean = methodSeq(Skill::adaptiveDamage).applyAll().any { it }
    val denyAdaptiveDamage: Boolean = methodSeq(Skill::denyAdaptiveDamage).applyAll().any { it }

    val neutralizeFollowUp: Sequence<Boolean>
        get() = methodSeq(Skill::neutralizeFollowUp).applyAll()
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
    val cooldownBuff: Sequence<InCombatSkill<CooldownChange<Int>?>>
        get() = methodSeq(Skill::cooldownBuff)
    val cooldownDebuff: Sequence<InCombatSkill<CooldownChange<Int>?>>
        get() = methodSeq(Skill::cooldownDebuff)
    val triangleAdept: Sequence<InCombatSkill<Int>>
        get() = methodSeq(Skill::triangleAdept)
    val cancelAffinity: Sequence<InCombatSkill<Int>>
        get() = methodSeq(Skill::cancelAffinity)


    // per attack
    val percentageDamageReduce: Sequence<(CombatStatus<InCombatStat>, specialTriggered: Boolean) -> Int>
        get() = methodSeq(Skill::percentageDamageReduce)
    val flatDamageReduce: Sequence<(CombatStatus<InCombatStat>, specialTriggered: Boolean) -> Int>
        get() = methodSeq(Skill::flatDamageReduce)
    val damageIncrease: Sequence<(CombatStatus<InCombatStat>, specialTriggered: Boolean) -> Int>
        get() = methodSeq(Skill::damageIncrease)

    // listener
    val damageDealtListener: Sequence<PerAttackListener<DamageDealt>>
        get() = methodSeq(Skill::damageDealtListener)
    val damageReceivedListener: Sequence<PerAttackListener<DamageDealt>>
        get() = methodSeq(Skill::damageReceivedListener)


    val postCombat: Sequence<CombatEndSkill>
        get() = methodSeq(Skill::combatEnd)
}