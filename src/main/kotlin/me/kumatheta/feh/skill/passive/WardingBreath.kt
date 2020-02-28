package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.CombatStartSkill
import me.kumatheta.feh.CooldownChange
import me.kumatheta.feh.InCombatSkill
import me.kumatheta.feh.Passive
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.effect.blowOrStance

object WardingBreath : Passive {
    override val inCombatStat: CombatStartSkill<Stat>? = blowOrStance(Stat.ZERO, Stat(res = 4))

    override val cooldownBuff: InCombatSkill<CooldownChange<Int>>? = {
        if (it.initAttack) {
            CooldownChange(0, 0)
        } else {
            CooldownChange(1, 1)
        }
    }
}