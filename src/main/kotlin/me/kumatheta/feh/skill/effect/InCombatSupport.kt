package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.AttackerDefenderPair
import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit

interface InCombatSupport : SkillEffect {
    fun getSupportSkills(inCombatSupportInput: InCombatSupportInput): AttackerDefenderPair<Sequence<InCombatSkillEffect>>
}

data class InCombatSupportInput(
    val battleState: BattleState,
    val self: HeroUnit,
    val attacker: HeroUnit,
    val defender: HeroUnit
) {
    val allyIsAttacker = self.team == attacker.team
    val targetAlly
        get() = if (allyIsAttacker) {
            attacker
        } else {
            defender
        }
    val targetFoe
        get() = if (allyIsAttacker) {
            defender
        } else {
            attacker
        }
}

fun InCombatSupportInput.forAlly(skillEffect: InCombatSkillEffect): AttackerDefenderPair<Sequence<InCombatSkillEffect>> {
    return forAlly(sequenceOf(skillEffect))
}

fun InCombatSupportInput.forAlly(skills: Sequence<InCombatSkillEffect>): AttackerDefenderPair<Sequence<InCombatSkillEffect>> {
    return forAllyAndFoe(skills, emptySequence())
}

fun InCombatSupportInput.forFoe(skills: Sequence<InCombatSkillEffect>): AttackerDefenderPair<Sequence<InCombatSkillEffect>> {
    return forAllyAndFoe(emptySequence(), skills)
}

fun InCombatSupportInput.forAllyAndFoe(
    forAlly: Sequence<InCombatSkillEffect>,
    forFoe: Sequence<InCombatSkillEffect>
): AttackerDefenderPair<Sequence<InCombatSkillEffect>> {
    return if (allyIsAttacker) {
        AttackerDefenderPair(forAlly, forFoe)
    } else {
        AttackerDefenderPair(forFoe, forAlly)
    }
}

private val EmptyAttackerDefenderSeq: AttackerDefenderPair<Sequence<InCombatSkillEffect>> =
    AttackerDefenderPair(emptySequence(), emptySequence())

fun emptyAttackerDefenderSequences(): AttackerDefenderPair<Sequence<InCombatSkillEffect>> =
    EmptyAttackerDefenderSeq


