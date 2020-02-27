package me.kumatheta.feh.skill

import me.kumatheta.feh.CombatEndSkill
import me.kumatheta.feh.NegativeStatus

fun negativeStatus(negativeStatus: NegativeStatus, range: Int, applyToTarget: Boolean): CombatEndSkill? {
    return { combatStatus, _ ->
        if (applyToTarget) {
            combatStatus.foe.heroUnit.addNegativeStatus(negativeStatus)
        }
        if (range > 0) {
            combatStatus.foe.heroUnit.nearbyAllies(combatStatus.battleState, range).forEach {
                it.addNegativeStatus(negativeStatus)
            }
        }
    }
}