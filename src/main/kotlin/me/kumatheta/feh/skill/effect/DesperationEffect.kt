package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.CombatStatus

interface DesperationEffect : CombatStartEffect<Boolean>, InCombatSkillEffect

class Desperation(private val percentage: Int) : DesperationEffect {
    override fun apply(combatStatus: CombatStatus<HeroUnit>): Boolean {
        return combatStatus.self.hpThreshold(percentage) <= 0
    }
}