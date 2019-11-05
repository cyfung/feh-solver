package com.bloombase.feh.skill.assist

import com.bloombase.feh.CombatResult
import com.bloombase.feh.HeroUnit
import com.bloombase.feh.NormalAssist

val HEAL_COMPARATOR = compareBy<Pair<HeroUnit, Int>>({
    it.second
}, {
    it.first.currentStatTotal
}, {
    it.first.id
})

fun Sequence<Pair<HeroUnit, Int>>.bestHealTarget(): HeroUnit? {
    return maxWith(HEAL_COMPARATOR)?.first
}

abstract class Heal(private val threshold: Int) : NormalAssist() {
    final override fun isValidPreCombat(
        self: HeroUnit,
        selfAttacks: List<CombatResult>
    ): Boolean {
        return true
    }

    final override fun preCombatBestTarget(self: HeroUnit, targets: Set<HeroUnit>): HeroUnit? {
        return targets.asSequence().map { target ->
            target to healAmount(self, target)
        }.filter {
            it.second >= threshold
        }.bestHealTarget()
    }

    abstract fun healAmount(self: HeroUnit, target: HeroUnit): Int
}