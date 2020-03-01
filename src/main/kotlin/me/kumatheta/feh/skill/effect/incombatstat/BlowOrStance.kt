package me.kumatheta.feh.skill.effect.incombatstat

import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.BasicSkill

fun blowOrStance(blow: Stat, stance: Stat) = BasicSkill(
    inCombatStat = {
        if (it.initAttack) {
            blow
        } else {
            stance
        }
    }
)

fun blow(stat: Stat) = blowOrStance(stat, Stat.ZERO)
fun stance(stat: Stat) = blowOrStance(Stat.ZERO, stat)