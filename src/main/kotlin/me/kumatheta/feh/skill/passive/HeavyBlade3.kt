package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.skill.CooldownChange
import me.kumatheta.feh.skill.InCombatSkill

val HeavyBlade3: InCombatSkill<CooldownChange<Int>> = {
    if (it.self.inCombatStat.atk > it.foe.inCombatStat.atk) {
        CooldownChange(1, 0)
    } else {
        CooldownChange(0, 0)
    }
}
