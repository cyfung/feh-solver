package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.skill.CombatStartSkill

fun quickRiposte(percentage: Int): CombatStartSkill<Int> {
    return {
        if (!it.initAttack && it.self.hpThreshold(percentage) >= 0) {
            1
        } else {
            0
        }
    }
}
