package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.skill.CooldownChange
import me.kumatheta.feh.skill.InCombatSkill

val Guard3: InCombatSkill<CooldownChange<Int>> = {
    if (it.self.heroUnit.hpThreshold(80) >= 0) {
        CooldownChange(1, 1)
    } else {
        CooldownChange(0, 0)
    }
}


