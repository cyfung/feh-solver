package me.kumatheta.feh.skill.assist

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Position

private const val THRESHOLD = 10

abstract class RestoreAssist(private val baseHeal: Int) : me.kumatheta.feh.NormalAssist() {
    override fun apply(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState
    ) {
        applyHeal(self, target, battleState, healAmount(baseHeal, self, target))
        target.clearPenalty()
    }

    override fun isValidAction(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState,
        fromPosition: Position
    ): Boolean {
        return target.currentHp < target.stat.hp || target.debuff.isNotZero()
    }

    override fun preCombatBestTarget(
        self: HeroUnit,
        targets: Set<HeroUnit>,
        lazyAllyThreat: Lazy<Map<HeroUnit, Set<HeroUnit>>>,
        distanceToClosestFoe: Map<HeroUnit, Int>
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