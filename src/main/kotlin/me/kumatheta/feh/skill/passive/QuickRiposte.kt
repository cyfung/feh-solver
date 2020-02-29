package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.CombatStartSkill
import me.kumatheta.feh.InCombatSkill

fun quickRiposte(percentage: Int): CombatStartSkill<Int> {
    return {
        if (!it.initAttack && it.self.hpThreshold(percentage) >= 0) {
            1
        } else {
            0
        }
    }
}
