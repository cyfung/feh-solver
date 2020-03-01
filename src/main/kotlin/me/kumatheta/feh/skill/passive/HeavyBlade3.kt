package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.CooldownChange

val HeavyBlade3 = BasicSkill(
    cooldownBuff = {
        if (it.self.inCombatStat.atk > it.foe.inCombatStat.atk) {
            CooldownChange(1, 0)
        } else {
            CooldownChange(0, 0)
        }
    }
)