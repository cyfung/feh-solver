package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.combatStartSkill

private val FOE_EFFECT = BasicSkill(counter = combatStartSkill(-1))

fun windsweep(minDiff: Int) = BasicSkill(
    followUp = {
        if (it.initAttack) {
            -1
        } else {
            0
        }
    },
    foeEffect = {
        if (it.initAttack && !it.foe.weaponType.targetRes && it.self.virtualSpd >= it.foe.virtualSpd + minDiff) {
            FOE_EFFECT
        } else {
            null
        }
    }
)