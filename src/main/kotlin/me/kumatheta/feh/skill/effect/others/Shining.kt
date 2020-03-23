package me.kumatheta.feh.skill.effect.others

import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.InCombatStat
import me.kumatheta.feh.skill.effect.DamageIncrease

object ShiningEffect : DamageIncrease {
    override fun getDamageIncrease(combatStatus: CombatStatus<InCombatStat>, specialTriggered: Boolean): Int {
        return if (combatStatus.foe.inCombatStat.def >= combatStatus.foe.inCombatStat.res + 5) {
            7
        } else {
            0
        }
    }

}