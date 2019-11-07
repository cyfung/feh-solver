package com.bloombase.feh.skill.assist

import com.bloombase.feh.HeroUnit
import com.bloombase.feh.NormalAssist
import com.bloombase.feh.Stat

private const val THRESHOLD = 10

abstract class RestoreAssist(private val baseHeal: Int) : NormalAssist() {
    override fun preCombatBestTarget(
        self: HeroUnit,
        targets: Set<HeroUnit>,
        lazyAllyThreat: Lazy<Set<HeroUnit>>,
        distanceToClosestEnemy: Map<HeroUnit, Int>
    ): HeroUnit? {
        val (noDebuff, hasDebuff) = targets.partition { it.debuff.isZero() }
        return preCombatNoDebuffBestTarget(self, noDebuff) ?: preCombatHasDebuffBestTarget(hasDebuff)
    }

    private fun preCombatHasDebuffBestTarget(
        targets: List<HeroUnit>
    ): HeroUnit? {
        return targets.maxBy { it.id }
    }

    private fun preCombatNoDebuffBestTarget(
        self: HeroUnit,
        targets: List<HeroUnit>
    ): HeroUnit? {
        return targets.asSequence().map { target ->
            target to healAmount(baseHeal, self, target)
        }.filter {
            it.second >= THRESHOLD
        }.bestHealTarget()
    }

}