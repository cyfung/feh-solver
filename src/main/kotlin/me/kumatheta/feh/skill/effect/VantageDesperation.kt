package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.CombatStartSkill

fun belowThreshold(percentage: Int): CombatStartSkill<Boolean> = {
    it.self.hpThreshold(percentage) <= 0
}