package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.CombatStatus

interface AdaptiveDamageEffect : CombatStartEffect<Boolean>, SkillEffect

interface DenyAdaptiveDamageEffect : CombatStartEffect<Boolean>, SkillEffect

object AdaptiveDamageEffectBasic : AdaptiveDamageEffect {
    override fun apply(combatStatus: CombatStatus<HeroUnit>): Boolean {
        return true
    }
}

object DenyAdaptiveDamageEffectBasic : DenyAdaptiveDamageEffect {
    override fun apply(combatStatus: CombatStatus<HeroUnit>): Boolean {
        return true
    }
}