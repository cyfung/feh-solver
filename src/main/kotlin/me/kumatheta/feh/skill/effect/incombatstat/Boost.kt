package me.kumatheta.feh.skill.effect.incombatstat

import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.BasicSkill

fun boost(stat: Stat) = BasicSkill(
    inCombatStat = { combatStatus ->
        if (combatStatus.self.currentHp >= combatStatus.foe.currentHp + 3) {
            stat
        } else {
            Stat.ZERO
        }
    }
)
