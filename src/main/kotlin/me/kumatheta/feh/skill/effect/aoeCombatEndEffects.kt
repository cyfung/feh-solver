package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.NegativeStatus
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.CombatEndSkill
import me.kumatheta.feh.skill.nearbyAllies

fun aoeNegativeStatus(negativeStatus: NegativeStatus, range: Int, applyToTarget: Boolean): CombatEndSkill {
    return aoeEnemy(range, applyToTarget) {
        it.addNegativeStatus(negativeStatus)
    }
}

fun aoeDebuff(range: Int, applyToTarget: Boolean, debuff: Stat): CombatEndSkill {
    return aoeEnemy(range, applyToTarget) {
        it.applyDebuff(debuff)
    }
}

inline fun aoeEnemy(range: Int, applyToTarget: Boolean, crossinline f: (HeroUnit) -> Unit): CombatEndSkill {
    return { combatStatus, attacked ->
        if (attacked) {
            if (applyToTarget) {
                f(combatStatus.foe.heroUnit)
            }
            if (range > 0) {
                combatStatus.foe.heroUnit.nearbyAllies(combatStatus.battleState, range).forEach {
                    f(it)
                }
            }
        }
    }
}