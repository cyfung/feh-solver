package me.kumatheta.feh.skill

import me.kumatheta.feh.InCombatSkill
import me.kumatheta.feh.Passive

fun belowThreshold(percentage: Int): InCombatSkill<Boolean> = {
    it.self.heroUnit.hpThreshold(percentage) <= 0
}