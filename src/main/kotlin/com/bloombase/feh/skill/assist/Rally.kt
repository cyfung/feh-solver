package com.bloombase.feh.skill.assist

import com.bloombase.feh.*

abstract class Rally(private val bonus: Stat) : NormalAssist() {
    override fun isValidPreCombat(self: HeroUnit, selfAttacks: List<CombatResult>): Boolean {
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

    override fun preCombatBestTarget(
        self: HeroUnit,
        targets: Set<HeroUnit>,
        lazyAllyThreat: Lazy<Set<HeroUnit>>,
        distanceToClosestEnemy: Map<HeroUnit, Int>
    ): HeroUnit? {
        return targets.intersect(lazyAllyThreat.value).asSequence().map {
            it to it.stat.rallyGain(bonus)
        }.filter {
            it.second >= 2
        }.minWith(
            compareByDescending<Pair<HeroUnit,Int>> { it.second }.thenBy {
                distanceToClosestEnemy[it.first]
            }.thenBy(BASE_ASSIST_COMPARATOR) {
                it.first
            }
        )?.first
    }
}