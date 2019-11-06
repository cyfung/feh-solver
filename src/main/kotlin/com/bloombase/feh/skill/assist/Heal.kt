package com.bloombase.feh.skill.assist

import com.bloombase.feh.CombatResult
import com.bloombase.feh.HeroUnit
import com.bloombase.feh.NormalAssist
import com.bloombase.feh.skill.special.Imbue
import com.bloombase.feh.util.compareByDescending

val BASE_ASSIST_COMPARATOR = compareByDescending<HeroUnit>({
    it.currentStatTotal
}, {
    it.id
})

val HEAL_COMPARATOR = compareByDescending<Pair<HeroUnit, Int>> {
    it.second
}.thenBy(BASE_ASSIST_COMPARATOR) { it.first }

fun Sequence<Pair<HeroUnit, Int>>.bestHealTarget(): HeroUnit? {
    return minWith(HEAL_COMPARATOR)?.first
}

fun healAmount(baseHeal: Int, self: HeroUnit, target: HeroUnit): Int {
    val maxHealAmount = if(self.special == Imbue && self.cooldownCount == 0) {
        baseHeal + Imbue.healBonus
    } else {
        baseHeal
    }
    return Integer.min(maxHealAmount, target.stat.hp - target.currentHp)
}

abstract class Heal(private val threshold: Int) : NormalAssist() {
    final override fun isValidPreCombat(
        self: HeroUnit,
        selfAttacks: List<CombatResult>
    ): Boolean {
        return true
    }

    final override fun preCombatBestTarget(
        self: HeroUnit,
        targets: Set<HeroUnit>,
        lazyAllyThreat: Lazy<Set<HeroUnit>>,
        distanceToClosestEnemy: Map<HeroUnit, Int>
    ): HeroUnit? {
        return targets.asSequence().map { target ->
            target to healAmount(self, target)
        }.filter {
            it.second >= threshold
        }.bestHealTarget()
    }

    abstract fun healAmount(self: HeroUnit, target: HeroUnit): Int
}