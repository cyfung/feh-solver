package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.CombatStatus

interface EffectOnFoe : SkillEffect, CombatStartEffect<Sequence<SkillEffect>>

class EffectOnFoeBasic(private val skillEffects: List<SkillEffect>) : EffectOnFoe {
    override fun apply(combatStatus: CombatStatus<HeroUnit>): Sequence<SkillEffect> {
        return skillEffects.asSequence()
    }
}