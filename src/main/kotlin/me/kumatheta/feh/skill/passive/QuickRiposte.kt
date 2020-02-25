package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.InCombatSkill
import me.kumatheta.feh.Passive

fun quickRiposte(percentage: Int): InCombatSkill<Int> {
    return {
        if (it.self.heroUnit.hpThreshold(percentage) >= 0) {
            1
        } else {
            0
        }
    }
}
