package me.kumatheta.feh.skill.assist

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.CombatResult
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Position
import kotlin.math.min

object Sacrifice : me.kumatheta.feh.NormalAssist() {
    override fun apply(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState
    ) {
        val heal = healAmount(target, self)
        if (heal > 0) {
            target.heal(heal)
            self.takeNonLethalDamage(heal)
        }
        target.applyBuff(-target.debuff)
        target.clearPenalty()
    }

    override fun isValidAction(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState,
        fromPosition: Position
    ): Boolean {
        return target.debuff.isNotZero() || healAmount(target, self) > 0
    }

    override fun isValidPreCombat(
        self: HeroUnit,
        selfAttacks: List<CombatResult>
    ): Boolean {
        return selfAttacks.all {
            it.potentialDamage < 5
        }
    }

    override fun preCombatBestTarget(
        self: HeroUnit,
        targets: Set<HeroUnit>,
        lazyAllyThreat: Lazy<Set<HeroUnit>>,
        distanceToClosestFoe: Map<HeroUnit, Int>
    ): HeroUnit? {
        return targets.asSequence().map { target ->
            target to healAmount(target, self)
        }.filter {
            it.second > 0
        }.bestHealTarget()
    }

    override fun postCombatBestTarget(
        self: HeroUnit,
        targets: Set<HeroUnit>,
        lazyAllyThreat: Lazy<Set<HeroUnit>>,
        foeThreat: Map<Position, Int>,
        distanceToClosestFoe: Map<HeroUnit, Int>,
        battleState: BattleState
    ): HeroUnit? {
        return targets.asSequence().map { target ->
            target to healAmount(target, self)
        }.bestHealTarget()
    }

    private fun healAmount(target: HeroUnit, self: HeroUnit) =
        min(target.stat.hp - target.currentHp, self.currentHp - 1)
}