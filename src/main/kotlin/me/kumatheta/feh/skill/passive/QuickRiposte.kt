package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.skill.BasicSkill

fun quickRiposte(percentage: Int) = BasicSkill(
    followUp = {
        if (!it.initAttack && it.self.hpThreshold(percentage) >= 0) {
            1
        } else {
            0
        }
    }
)