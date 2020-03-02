package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.CooldownChange

private val COOLDOWN_CHANGE = CooldownChange(1, 0)

val BoldFighter3 = BasicSkill(
    followUp = {
        if (it.initAttack) {
            1
        } else {
            0
        }
    },
    cooldownBuff = {
        if (it.initAttack) {
            COOLDOWN_CHANGE
        } else {
            null
        }
    }
)