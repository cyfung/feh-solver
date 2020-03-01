package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.*
import me.kumatheta.feh.skill.effect.stance

object WardingBreath : BasicSkill() {
    override val inCombatStat: CombatStartSkill<Stat>? = stance(Stat(res = 4))

    override val cooldownBuff: InCombatSkill<CooldownChange<Int>>? = {
        if (it.initAttack) {
            CooldownChange(0, 0)
        } else {
            CooldownChange(1, 1)
        }
    }
}