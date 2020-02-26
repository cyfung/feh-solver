package me.kumatheta.feh.skill.assist

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.CombatResult
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Position

object ReciprocalAid : me.kumatheta.feh.NormalAssist() {
    override fun apply(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState
    ) {
        val healAmount = target.currentHp - self.currentHp
        if (healAmount > 0) {
            target.heal(healAmount)
            self.takeNonLethalDamage(healAmount)
        } else {
            target.takeNonLethalDamage(-healAmount)
            self.heal(-healAmount)
        }
    }

    override fun isValidAction(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState,
        fromPosition: Position
    ): Boolean {
        return (target.currentHp < target.maxHp && self.currentHp > target.currentHp) ||
            (self.currentHp < self.maxHp && target.currentHp > self.currentHp)
    }

    override fun isValidPreCombat(
        self: HeroUnit,
        selfAttacks: List<CombatResult>
    ): Boolean {
        return self.isEmptyHanded
    }

    override fun preCombatBestTarget(
        self: HeroUnit,
        targets: Set<HeroUnit>,
        lazyAllyThreat: Lazy<Map<HeroUnit, Set<HeroUnit>>>,
        distanceToClosestFoe: Map<HeroUnit, Int>
    ): HeroUnit? {
        return bestTarget(self, targets)
    }

    override fun postCombatBestTarget(
        self: HeroUnit,
        targets: Set<HeroUnit>,
        lazyAllyThreat: Lazy<Map<HeroUnit, Set<HeroUnit>>>,
        foeThreat: Map<Position, Int>,
        distanceToClosestFoe: Map<HeroUnit, Int>,
        battleState: BattleState
    ): HeroUnit? {
        return bestTarget(self, targets)
    }

    private fun bestTarget(
        self: HeroUnit,
        targets: Set<HeroUnit>
    ): HeroUnit? {
        return targets.asSequence().filter { target ->
            target.maxHp > target.currentHp &&
                self.currentHp > target.currentHp &&
                target.maxHp >= self.currentHp &&
                self.maxHp >= target.currentHp
        }.map { target ->
            target to (self.currentHp - target.currentHp)
        }.bestHealTarget()
    }
}
