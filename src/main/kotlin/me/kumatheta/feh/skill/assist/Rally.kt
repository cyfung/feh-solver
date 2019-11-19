package me.kumatheta.feh.skill.assist

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Position
import me.kumatheta.feh.Stat

abstract class Rally(private val bonus: Stat) : BuffRelatedAssist() {
    final override fun apply(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState
    ) {
        target.applyBuff(bonus)
    }

    final override fun isValidAction(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState,
        fromPosition: Position
    ): Boolean {
        return target.buff.rallyGain(bonus) > 0
    }

    final override fun preCombatBestTarget(
        self: HeroUnit,
        targets: Set<HeroUnit>,
        lazyAllyThreat: Lazy<Set<HeroUnit>>,
        distanceToClosestFoe: Map<HeroUnit, Int>
    ): HeroUnit? {
        val allyThreat = lazyAllyThreat.value
        return targets.asSequence().filter { it.available }.filter {
            allyThreat.contains(it)
        }.map {
            it to it.stat.rallyGain(bonus)
        }.filter {
            it.second >= 2
        }.minWith(
            targetComparator(distanceToClosestFoe)
        )?.first
    }


}

