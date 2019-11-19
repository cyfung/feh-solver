package me.kumatheta.feh.skill.assist

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.CombatResult
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Position
import me.kumatheta.feh.skill.special.Imbue

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
    override fun apply(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState
    ) {
        target.heal(healAmount(self, target))
    }

    override fun isValidAction(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState,
        fromPosition: Position
    ): Boolean {
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
        lazyAllyThreat: Lazy<Map<HeroUnit, Set<HeroUnit>>>,
        distanceToClosestFoe: Map<HeroUnit, Int>
    ): HeroUnit? {
        return targets.asSequence().map { target ->
            target to healAmount(self, target)
        }.filter {
            it.second >= threshold
        }.bestHealTarget()
    }

    override fun postCombatBestTarget(
        self: HeroUnit,
        targets: Set<HeroUnit>,
        lazyAllyThreat: Lazy<Map<HeroUnit, Set<HeroUnit>>>,
        foeThreat: Map<Position, Int>,
        distanceToClosestFoe: Map<HeroUnit, Int>,
        battleState: BattleState
    ): HeroUnit? {
        return targets.asSequence().map { target ->
            target to healAmount(self, target)
        }.bestHealTarget()
    }

    abstract fun healAmount(self: HeroUnit, target: HeroUnit): Int
}