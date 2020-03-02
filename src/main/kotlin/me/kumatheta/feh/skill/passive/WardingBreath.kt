package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.CooldownChange
import me.kumatheta.feh.skill.effect.incombatstat.stance

private val COOLDOWN_CHANGE = CooldownChange(1, 1)

val WardingBreath = BasicSkill(
    cooldownBuff = {
        if (it.initAttack) {
            null
        } else {
            COOLDOWN_CHANGE
        }
    },
    inCombatStat = stance(Stat(res = 4)).inCombatStat!!
)