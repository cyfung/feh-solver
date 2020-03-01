package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.skill.CombatStartSkill
import me.kumatheta.feh.Stat

fun boost(stat: Stat): CombatStartSkill<Stat> {
    return { combatStatus ->
        if (combatStatus.self.currentHp >= combatStatus.foe.currentHp + 3) {
            stat
        } else {
            Stat.ZERO
        }
    }
}
