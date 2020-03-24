package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.CombatStatus

class TriangleAdept(private val value: Int) : CombatStartEffect<Int>, InCombatSkillEffect {
    override fun apply(combatStatus: CombatStatus<HeroUnit>): Int {
        return value
    }
}