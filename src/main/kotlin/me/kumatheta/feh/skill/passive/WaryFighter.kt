package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.skill.*

private val FOE_EFFECT = BasicSkill(followUp = combatStartSkill(-1))

fun waryFighter(percentageHp: Int) = BasicSkill(
    followUp = {
        if (it.self.hpThreshold(percentageHp) >= 0) {
            -1
        } else {
            0
        }
    },
    foeEffect = {
        if (it.self.hpThreshold(percentageHp) >= 0) {
            FOE_EFFECT
        } else {
            null
        }
    }
)