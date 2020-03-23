package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.CombatStatus

interface VantageEffect : CombatStartEffect<Boolean>, SkillEffect

class Vantage(private val percentage: Int) : VantageEffect {
    override fun apply(combatStatus: CombatStatus<HeroUnit>): Boolean {
        return combatStatus.self.hpThreshold(percentage) <= 0
    }
}