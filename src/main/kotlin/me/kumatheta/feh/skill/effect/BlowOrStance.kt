package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.skill.CombatStartSkill
import me.kumatheta.feh.Stat

fun blowOrStance(blow: Stat, stance: Stat): CombatStartSkill<Stat> {
    return {
        if (it.initAttack) {
            blow
        } else {
            stance
        }
    }
}

fun blow(stat: Stat) = blowOrStance(stat, Stat.ZERO)
fun stance(stat: Stat) = blowOrStance(Stat.ZERO, stat)