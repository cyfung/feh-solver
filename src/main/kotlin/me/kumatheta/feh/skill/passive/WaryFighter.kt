package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.skill.*

private val FOE_EFFECT = combatStartSkill(-1).toFollowUpPassive()

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