package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.CooldownChange

private val COOLDOWN_CHANGE = CooldownChange(1, 0)

val HeavyBlade3 = BasicSkill(
    cooldownBuff = {
        if (it.self.inCombatStat.atk > it.foe.inCombatStat.atk) {
            COOLDOWN_CHANGE
        } else {
            null
        }
    }
)