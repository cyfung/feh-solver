package me.kumatheta.feh.skill.assist

import me.kumatheta.feh.CombatResult
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.NormalAssist
import me.kumatheta.feh.skill.special.Imbue
import me.kumatheta.feh.util.compareByDescending

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

abstract class Heal(private val threshold: Int) : me.kumatheta.feh.NormalAssist() {
    override fun apply(self: HeroUnit, target: HeroUnit) {
        target.heal(healAmount(self, target))
    }

    override fun isValidAction(self: HeroUnit, target: HeroUnit): Boolean {
        return target.currentHp < target.stat.hp
    }

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