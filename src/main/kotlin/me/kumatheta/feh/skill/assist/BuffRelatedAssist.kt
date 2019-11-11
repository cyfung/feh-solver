package me.kumatheta.feh.skill.assist

import me.kumatheta.feh.CombatResult
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.WinLoss

abstract class BuffRelatedAssist : me.kumatheta.feh.NormalAssist() {
    final override fun isValidPreCombat(self: HeroUnit, selfAttacks: List<CombatResult>): Boolean {
        return if (self.hasSpecialDebuff) {
            selfAttacks.all {
                it.potentialDamage < 5 && it.debuffSuccess == 0 && it.winLoss == WinLoss.LOSS
            }
        } else {
            selfAttacks.all {
                it.potentialDamage < 5 && it.debuffSuccess == 0
            }
        }
    }

    fun targetComparator(distanceToClosestEnemy: Map<HeroUnit, Int>): Comparator<Pair<HeroUnit, Int>> {
        return compareByDescending<Pair<HeroUnit, Int>> { it.second }.thenBy {
            distanceToClosestEnemy[it.first]
        }.thenBy(BASE_ASSIST_COMPARATOR) {
            it.first
        }
    }
}