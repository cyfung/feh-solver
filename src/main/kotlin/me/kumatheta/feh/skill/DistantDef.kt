package me.kumatheta.feh.skill

import me.kumatheta.feh.CombatStartSkill
import me.kumatheta.feh.Stat

fun rangeDefStat(stat: Stat): CombatStartSkill<Stat> {
    return {
        if (!it.initAttack || it.foe.weaponType.isRanged) {
            stat
        } else {
            Stat.ZERO
        }
    }
}