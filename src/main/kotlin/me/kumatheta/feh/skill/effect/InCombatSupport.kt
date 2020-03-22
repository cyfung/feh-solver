package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.AttackerDefenderPair
import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.Skill

interface InCombatSupport : SkillEffect {
    fun getSupportSkills(inCombatSupportInput: InCombatSupportInput): AttackerDefenderPair<Sequence<Skill>>
}

data class InCombatSupportInput(
    val battleState: BattleState,
    val self: HeroUnit,
    val attacker: HeroUnit,
    val defender: HeroUnit
) {
    private val allyIsAttacker = self.team == attacker.team
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