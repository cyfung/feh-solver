package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.CooldownChange
import me.kumatheta.feh.InCombatSkill
import me.kumatheta.feh.Passive

object Guard3 : Passive {
    override val cooldownDebuff: InCombatSkill<CooldownChange<Int>>? = {
        if (it.self.heroUnit.hpThreshold(80) >= 0) {
            CooldownChange(1, 1)
        } else {
            CooldownChange(0, 0)
        }
    }

}
