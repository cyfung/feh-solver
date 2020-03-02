package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.CooldownChange

private val COOLDOWN_CHANGE = CooldownChange(1, 0)

val FlashingBlade3 = BasicSkill(
    cooldownBuff = {
        if (it.self.inCombatStat.spd > it.foe.inCombatStat.spd) {
            COOLDOWN_CHANGE
        } else {
            null
        }
    }
)