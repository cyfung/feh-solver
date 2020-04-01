package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.CombatStatus

interface DisableFoeAllySupportEffect : CombatStartEffect<Boolean>, SkillEffect

object DisableFoeAllySupportEffectBasic: DisableFoeAllySupportEffect {
    override fun apply(combatStatus: CombatStatus<HeroUnit>): Boolean {
        return true
    }
}