package me.kumatheta.feh.skill.effect.supportincombat

import me.kumatheta.feh.AttackerDefenderPair
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.effect.InCombatSkillEffect
import me.kumatheta.feh.skill.effect.InCombatSupport
import me.kumatheta.feh.skill.effect.InCombatSupportInput
import me.kumatheta.feh.skill.effect.SkillEffect
import me.kumatheta.feh.skill.effect.emptyAttackerDefenderSequences
import me.kumatheta.feh.skill.effect.forAlly
import me.kumatheta.feh.skill.toInCombatStatEffect

class Spur(private val buff: InCombatSkillEffect) : InCombatSupport {
    override fun getSupportSkills(inCombatSupportInput: InCombatSupportInput): AttackerDefenderPair<Sequence<InCombatSkillEffect>> {
        return if (inCombatSupportInput.targetAlly.position.distanceTo(inCombatSupportInput.self.position) == 1) {
            inCombatSupportInput.forAlly(buff)
        } else {
            emptyAttackerDefenderSequences()
        }
    }
}

fun spur(stat: Stat): InCombatSupport = Spur(stat.toInCombatStatEffect())