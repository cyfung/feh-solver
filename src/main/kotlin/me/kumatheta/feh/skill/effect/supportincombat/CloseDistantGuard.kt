package me.kumatheta.feh.skill.effect.supportincombat

import me.kumatheta.feh.AttackerDefenderPair
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.effect.InCombatStatEffect
import me.kumatheta.feh.skill.effect.InCombatSupport
import me.kumatheta.feh.skill.effect.InCombatSupportInput
import me.kumatheta.feh.skill.effect.SkillEffect
import me.kumatheta.feh.skill.effect.emptyAttackerDefenderSequences
import me.kumatheta.feh.skill.effect.forAlly
import me.kumatheta.feh.skill.toInCombatStatEffect

class CloseDistantGuard(private val isRanged: Boolean, private val buff: InCombatStatEffect) : InCombatSupport {
    override fun getSupportSkills(inCombatSupportInput: InCombatSupportInput): AttackerDefenderPair<Sequence<SkillEffect>> {
        return if (inCombatSupportInput.targetFoe.weaponType.isRanged == isRanged &&
            inCombatSupportInput.targetAlly.position.distanceTo(inCombatSupportInput.self.position) <= 2
        ) {
            inCombatSupportInput.forAlly(sequenceOf(buff))
        } else {
            emptyAttackerDefenderSequences()
        }
    }

}

fun closeGuard(buff: Stat) = CloseDistantGuard(false, buff.toInCombatStatEffect())
fun distantGuard(buff: Stat) = CloseDistantGuard(true, buff.toInCombatStatEffect())