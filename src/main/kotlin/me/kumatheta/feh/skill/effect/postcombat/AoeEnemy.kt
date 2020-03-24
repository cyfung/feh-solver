package me.kumatheta.feh.skill.effect.postcombat

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.NegativeStatus
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.InCombatStat
import me.kumatheta.feh.skill.effect.PostCombatEffect
import me.kumatheta.feh.skill.nearbyAllies

fun aoeNegativeStatus(negativeStatus: NegativeStatus, range: Int, applyToTarget: Boolean): AoeEnemy {
    return AoeEnemy(range, applyToTarget) {
        it.addNegativeStatus(negativeStatus)
    }
}

fun aoeDebuff(range: Int, applyToTarget: Boolean, debuff: Stat): AoeEnemy {
    return AoeEnemy(range, applyToTarget) {
        it.cachedEffect.applyDebuff(debuff)
    }
}

class AoeEnemy(private val range: Int, private val applyToTarget: Boolean, private val f: (HeroUnit) -> Unit) :
    PostCombatEffect {
    override fun onCombatEnd(combatStatus: CombatStatus<InCombatStat>, attacked: Boolean) {
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

val trilemmaEffect = aoeNegativeStatus(
    negativeStatus = NegativeStatus.TRIANGLE,
    range = 2,
    applyToTarget = true
)
