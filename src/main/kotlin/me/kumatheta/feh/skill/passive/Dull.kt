package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.ALL_STAT_TYPES
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.effect.EffectOnFoe
import me.kumatheta.feh.skill.effect.InCombatSkillEffect
import me.kumatheta.feh.skill.effect.NeutralizeBonusBasic
import me.kumatheta.feh.skill.effect.SkillEffect

private val NEUTRALIZE_ALL_STAT = NeutralizeBonusBasic(ALL_STAT_TYPES)

object DullClose3 : EffectOnFoe {
    override fun apply(combatStatus: CombatStatus<HeroUnit>): Sequence<InCombatSkillEffect> {
        return if (combatStatus.foe.weaponType.isRanged) {
            emptySequence()
        } else {
            sequenceOf(NEUTRALIZE_ALL_STAT)
        }
    }
}

object DullRanged3 : EffectOnFoe {
    override fun apply(combatStatus: CombatStatus<HeroUnit>): Sequence<InCombatSkillEffect> {
        return if (combatStatus.foe.weaponType.isRanged) {
            sequenceOf(NEUTRALIZE_ALL_STAT)
        } else {
            emptySequence()
        }
    }
}