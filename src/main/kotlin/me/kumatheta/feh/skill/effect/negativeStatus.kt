package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.CombatEndSkill
import me.kumatheta.feh.NegativeStatus
import me.kumatheta.feh.skill.nearbyAllies

fun negativeStatus(negativeStatus: NegativeStatus, range: Int, applyToTarget: Boolean): CombatEndSkill? {
    return { combatStatus, attacked ->
        if (attacked) {
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
}