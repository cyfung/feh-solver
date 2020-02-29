package me.kumatheta.feh.skill.assist

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.CombatResult
import me.kumatheta.feh.HealingSpecial
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.NormalAssist
import me.kumatheta.feh.Position
import me.kumatheta.feh.skill.BASE_ASSIST_COMPARATOR
import me.kumatheta.feh.skill.special.Imbue

val HEAL_COMPARATOR = compareByDescending<Pair<HeroUnit, Int>> {
    it.second
}.thenBy(BASE_ASSIST_COMPARATOR) { it.first }

fun Sequence<Pair<HeroUnit, Int>>.bestHealTarget(): HeroUnit? {
    return minWith(HEAL_COMPARATOR)?.first
}

fun healAmount(baseHeal: Int, self: HeroUnit, target: HeroUnit): Int {
    val maxHealAmount = if (self.special == Imbue && self.cooldown == 0) {
        baseHeal + Imbue.healBonus
    } else {
        baseHeal
    }
    return Integer.min(maxHealAmount, target.maxHp - target.currentHp)
}

fun applyHeal(
    self: HeroUnit,
    target: HeroUnit,
    battleState: BattleState,
    healAmount: Int
) {
    target.heal(healAmount)
    val healingSpecial = self.special as? HealingSpecial
    if (healingSpecial != null && self.cooldown == 0) {
        healingSpecial.trigger(self, target, battleState)
        self.resetCooldown()
    } else {
        self.reduceCooldown(1)
    }
    self.skillSet.onHealOthers.forEach { it(battleState, self, target, healAmount)  }
}

abstract class Heal(private val threshold: Int, isRange: Boolean = false) : NormalAssist(isRange) {
    override fun apply(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState
    ) {
        applyHeal(self, target, battleState, healAmount(self, target))
    }

    override fun isValidAction(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState,
        fromPosition: Position
    ): Boolean {
        return target.currentHp < target.maxHp
    }

    override fun isValidPreCombat(
        self: HeroUnit,
        selfAttacks: List<CombatResult>
    ): Boolean {
        return true
    }

    override fun preCombatBestTarget(
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