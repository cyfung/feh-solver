package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.CooldownChange
import me.kumatheta.feh.skill.effect.incombatstat.stance

val WardingBreath = BasicSkill(
    cooldownBuff = {
        if (it.initAttack) {
            CooldownChange(0, 0)
        } else {
            CooldownChange(1, 1)
        }
    },
    inCombatStat = stance(Stat(res = 4)).inCombatStat!!
)