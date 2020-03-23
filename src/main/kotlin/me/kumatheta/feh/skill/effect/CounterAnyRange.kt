package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.CombatStatus

interface CounterAnyRange : CombatStartEffect<Boolean>, SkillEffect

object CounterAnyRangeBasic: CounterAnyRange {
    override fun apply(combatStatus: CombatStatus<HeroUnit>): Boolean {
        return true
    }
}