package com.bloombase.feh.skill.assist

import com.bloombase.feh.CombatResult
import com.bloombase.feh.HeroUnit
import com.bloombase.feh.NormalAssist

abstract class Refresh : NormalAssist() {
    final override fun isValidPreCombat(
        self: HeroUnit,
        selfAttacks: List<CombatResult>
    ): Boolean {
        return selfAttacks.all {
            it.potentialDamage < 5
        }
    }

    override fun preCombatBestTarget(
        self: HeroUnit,
        targets: Set<HeroUnit>,
        lazyAllyThreat: Lazy<Set<HeroUnit>>,
        distanceToClosestEnemy: Map<HeroUnit, Int>
    ): HeroUnit? {
        return targets.filterNot {
            it.available
        }.minWith(
            compareBy<HeroUnit> {
                if (lazyAllyThreat.value.contains(it)) {
                    0
                } else {
                    1
                }
            }.thenBy(BASE_ASSIST_COMPARATOR) {
                it
            }
        )
    }
}

