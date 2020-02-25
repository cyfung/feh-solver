package me.kumatheta.feh.skill

import me.kumatheta.feh.CombatStartSkill
import me.kumatheta.feh.Stat

fun bond(stat: Stat): CombatStartSkill<Stat> {
    return { combatStatus ->
        val (battleState, self) = combatStatus
        if (battleState.unitsSeq(self.team).filterNot { it == self }
                .any { it.position.distanceTo(self.position) == 1 }) {
            stat
        } else {
            Stat.ZERO
        }
    }
}
