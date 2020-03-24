package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.AttackerDefenderPair
import me.kumatheta.feh.MagicB
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.*
import me.kumatheta.feh.skill.effect.InCombatSkillEffect
import me.kumatheta.feh.skill.effect.InCombatStatEffect
import me.kumatheta.feh.skill.effect.InCombatStatEffectBasic
import me.kumatheta.feh.skill.effect.InCombatSupport
import me.kumatheta.feh.skill.effect.InCombatSupportInput
import me.kumatheta.feh.skill.effect.SkillEffect
import me.kumatheta.feh.skill.effect.emptyAttackerDefenderSequences
import me.kumatheta.feh.skill.effect.forAlly

private val BUFF = InCombatStatEffectBasic(Stat(atk = 3, spd = 3))

val FlowerOfJoy = MagicB.basic(14, res = 3) + object : InCombatSupport {
    override fun getSupportSkills(inCombatSupportInput: InCombatSupportInput): AttackerDefenderPair<Sequence<InCombatSkillEffect>> {
        return if (inCombatSupportInput.self.inCardinalDirection(inCombatSupportInput.targetAlly)) {
            inCombatSupportInput.forAlly(BUFF)
        } else {
            emptyAttackerDefenderSequences()
        }
    }
}
