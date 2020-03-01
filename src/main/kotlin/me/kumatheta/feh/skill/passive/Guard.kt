package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.CooldownChange
import me.kumatheta.feh.skill.InCombatSkill
import me.kumatheta.feh.skill.Passive

object Guard3 : BasicSkill() {
    override val cooldownDebuff: InCombatSkill<CooldownChange<Int>>? = {
        if (it.self.heroUnit.hpThreshold(80) >= 0) {
            CooldownChange(1, 1)
        } else {
            CooldownChange(0, 0)
        }
    }

}
