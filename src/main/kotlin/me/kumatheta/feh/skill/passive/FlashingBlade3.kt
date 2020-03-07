package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.CooldownChange
import me.kumatheta.feh.skill.inCombatVirtualSpd

private val COOLDOWN_CHANGE = CooldownChange(1, 0)

val FlashingBlade3 = BasicSkill(
    cooldownBuff = {
        if (it.self.inCombatVirtualSpd > it.foe.inCombatVirtualSpd) {
            COOLDOWN_CHANGE
        } else {
            null
        }
    }
)