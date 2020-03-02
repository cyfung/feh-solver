package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.CooldownChange

val FlashingBlade3 = BasicSkill(
    cooldownBuff = {
        if (it.self.inCombatStat.spd > it.foe.inCombatStat.spd) {
            CooldownChange(1, 0)
        } else {
            CooldownChange(0, 0)
        }
    }
)