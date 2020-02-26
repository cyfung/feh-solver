package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.InCombatSkill

fun quickRiposte(percentage: Int): InCombatSkill<Int> {
    return {
        if (!it.initAttack && it.self.heroUnit.hpThreshold(percentage) >= 0) {
            1
        } else {
            0
        }
    }
}
