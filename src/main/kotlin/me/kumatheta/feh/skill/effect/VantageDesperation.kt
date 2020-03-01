package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.CombatStartSkill

fun belowThreshold(percentage: Int): CombatStartSkill<Boolean> = {
    it.self.hpThreshold(percentage) <= 0
}

fun vantage(percentage: Int) = BasicSkill(
    vantage = belowThreshold(percentage)
)

fun desperation(percentage: Int) = BasicSkill(
    desperation = belowThreshold(percentage)
)