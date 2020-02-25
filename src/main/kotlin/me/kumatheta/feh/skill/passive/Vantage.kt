package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.InCombatSkill
import me.kumatheta.feh.Passive

fun vantage(percentage: Int): InCombatSkill<Boolean> = {
    it.self.heroUnit.hpThreshold(percentage) <= 0
}