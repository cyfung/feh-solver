package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.CombatStatus

interface EffectOnFoe : SkillEffect, CombatStartEffect<Sequence<InCombatSkillEffect>>

class EffectOnFoeBasic(private val skillEffects: List<InCombatSkillEffect>) : EffectOnFoe {
    override fun apply(combatStatus: CombatStatus<HeroUnit>): Sequence<InCombatSkillEffect> {
        return skillEffects.asSequence()
    }
}

object DisableFoeCounter : EffectOnFoe {
    override fun apply(combatStatus: CombatStatus<HeroUnit>): Sequence<InCombatSkillEffect> {
        return if (combatStatus.initAttack) {
            sequenceOf(DisableCounter)
        } else {
            emptySequence()
        }
    }
}