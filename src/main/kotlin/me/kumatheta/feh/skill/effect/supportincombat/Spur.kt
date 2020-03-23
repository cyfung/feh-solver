package me.kumatheta.feh.skill.effect.supportincombat

import me.kumatheta.feh.AttackerDefenderPair
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.effect.InCombatSupport
import me.kumatheta.feh.skill.effect.InCombatSupportInput
import me.kumatheta.feh.skill.effect.SkillEffect
import me.kumatheta.feh.skill.effect.emptyAttackerDefenderSequences
import me.kumatheta.feh.skill.effect.forAlly
import me.kumatheta.feh.skill.effect.startofturn.Threaten
import me.kumatheta.feh.skill.toInCombatStatEffect

class Spur(private val buff: SkillEffect) : InCombatSupport {
    override fun getSupportSkills(inCombatSupportInput: InCombatSupportInput): AttackerDefenderPair<Sequence<SkillEffect>> {
        return if (inCombatSupportInput.targetAlly.position.distanceTo(inCombatSupportInput.self.position) == 1) {
            inCombatSupportInput.forAlly(buff)
        } else {
            emptyAttackerDefenderSequences()
        }
    }
}

fun spur(stat: Stat): InCombatSupport = Spur(stat.toInCombatStatEffect())

fun spur(atk: Int = 0,
             spd: Int = 0,
             def: Int = 0,
             res: Int = 0
) = spur(Stat(atk = atk, spd = spd, def = def, res = res))