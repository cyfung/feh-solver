package com.bloombase.feh.skill.assist

import com.bloombase.feh.HeroUnit
import com.bloombase.feh.NormalAssist
import com.bloombase.feh.Stat

private const val THRESHOLD = 10

abstract class RestoreAssist(private val baseHeal: Int) : NormalAssist() {
    override fun preCombatBestTarget(self: HeroUnit, targets: Set<HeroUnit>): HeroUnit? {
        val (noDebuff, hasDebuff) = targets.partition { it.debuff == Stat.ZERO }
        return preCombatNoDebuffBestTarget(self, noDebuff) ?: preCombatHasDebuffBestTarget(hasDebuff)
    }

    private fun preCombatHasDebuffBestTarget(
        targets: List<HeroUnit>
    ): HeroUnit? {
        return targets.minBy { it.id }
    }

    private fun preCombatNoDebuffBestTarget(
        self: HeroUnit,
        targets: List<HeroUnit>
    ): HeroUnit? {
        return targets.asSequence().map { target ->
            target to healAmount(self, target)
        }.filter {
            it.second >= THRESHOLD
        }.bestHealTarget()
    }

    private fun healAmount(self: HeroUnit, target: HeroUnit): Int {
        return baseHeal
    }
}