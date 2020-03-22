package me.kumatheta.feh.skill.effect.incombatstat

import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.BasicSkill

fun rangeDefStat(stat: Stat) = BasicSkill(
    inCombatStat = {
        if (!it.initAttack && it.foe.weaponType.isRanged) {
            stat
        } else {
            Stat.ZERO
        }
    }
)