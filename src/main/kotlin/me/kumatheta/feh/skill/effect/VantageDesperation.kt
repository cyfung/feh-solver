package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.InCombatSkill

fun belowThreshold(percentage: Int): InCombatSkill<Boolean> = {
    it.self.heroUnit.hpThreshold(percentage) <= 0
}