package me.kumatheta.feh.skill.assist

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat

abstract class Rally(private val bonus: Stat) : BuffRelatedAssist() {
    override fun apply(self: HeroUnit, target: HeroUnit) {
        target.applyBuff(bonus)
    }

    final override fun isValidAction(self: HeroUnit, target: HeroUnit): Boolean {
        return target.buff.rallyGain(bonus) > 0
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
            targetComparator(distanceToClosestEnemy)
        )?.first
    }

}

