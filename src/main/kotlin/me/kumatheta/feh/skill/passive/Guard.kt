package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.CooldownChange

val Guard3 = BasicSkill(
    cooldownDebuff = {
        if (it.self.heroUnit.hpThreshold(80) >= 0) {
            CooldownChange(1, 1)
        } else {
            CooldownChange(0, 0)
        }
    }
)


