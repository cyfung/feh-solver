package me.kumatheta.feh.skill.assist

import me.kumatheta.feh.CombatResult
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.WinLoss
import me.kumatheta.feh.skill.BASE_ASSIST_COMPARATOR
import me.kumatheta.feh.skill.NormalAssist

abstract class BuffRelatedAssist : NormalAssist() {
    final override fun isValidPreCombat(self: HeroUnit, selfAttacks: List<CombatResult>): Boolean {
        return if (self.specialDebuffer != null) {
            selfAttacks.all {
                it.potentialDamage < 5 && !it.debuffSuccess && it.winLoss == WinLoss.LOSS
            }
        } else {
            selfAttacks.all {
                it.potentialDamage < 5 && !it.debuffSuccess
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