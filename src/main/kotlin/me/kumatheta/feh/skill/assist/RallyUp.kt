package me.kumatheta.feh.skill.assist

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat

abstract class RallyUp(private val bonus: Stat) : BuffRelatedAssist() {
    override fun preCombatBestTarget(
        self: HeroUnit,
        targets: Set<HeroUnit>,
        lazyAllyThreat: Lazy<Set<HeroUnit>>,
        distanceToClosestEnemy: Map<HeroUnit, Int>
    ): HeroUnit? {
        val allyThreat = lazyAllyThreat.value
       return targets.asSequence().map {
            it to allyThreat.count { ally -> ally.position.distanceTo(it.position) <= 2 }
        }.filter {
            it.second > 0
        }.minWith(
            targetComparator(distanceToClosestEnemy)
        )?.first
    }

}

