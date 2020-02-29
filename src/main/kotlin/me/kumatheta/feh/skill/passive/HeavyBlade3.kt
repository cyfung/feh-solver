package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.CombatStartSkill
import me.kumatheta.feh.CooldownChange
import me.kumatheta.feh.InCombatSkill
import me.kumatheta.feh.Passive
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.effect.blowOrStance
import me.kumatheta.feh.skill.effect.stance

object HeavyBlade3 : Passive {
    override val cooldownBuff: InCombatSkill<CooldownChange<Int>>? = {
        if (it.self.inCombatStat.atk > it.foe.inCombatStat.atk) {
            CooldownChange(1, 0)
        } else {
            CooldownChange(0, 0)
        }
    }
}