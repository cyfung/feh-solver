package me.kumatheta.feh.skill.assist

import me.kumatheta.feh.*

private const val THRESHOLD = 10

abstract class RestoreAssist : Heal(10) {
    override fun apply(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState
    ) {
        super.apply(self, target, battleState)
        target.clearPenalty()
    }

    override fun isValidAction(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState,
        fromPosition: Position
    ): Boolean {
        return target.currentHp < target.maxHp || target.debuff.isNotZero()
    }

    override fun isValidPreCombat(self: HeroUnit, selfAttacks: List<CombatResult>): Boolean {
        return true
    }

    override fun preCombatBestTarget(
        self: HeroUnit,
        targets: Set<HeroUnit>,
        possibleAttacks: Map<HeroUnit, List<CombatResult>>,
        lazyAllyThreat: Lazy<Map<HeroUnit, Set<HeroUnit>>>,
        distanceToClosestFoe: Map<HeroUnit, Int>
    ): HeroUnit? {
        val (noDebuff, hasDebuff) = targets.partition { it.debuff.isZero() && !it.hasNegativeStatus }
        return preCombatNoDebuffBestTarget(self, noDebuff) ?: withDebuffBestTarget(hasDebuff)
    }

    override fun postCombatBestTarget(
        self: HeroUnit,
        targets: Set<HeroUnit>,
        lazyAllyThreat: Lazy<Map<HeroUnit, Set<HeroUnit>>>,
        foeThreat: Map<Position, Int>,
        distanceToClosestFoe: Map<HeroUnit, Int>,
        battleState: BattleState
    ): HeroUnit? {
        val (noDebuff, withDebuff) = targets.partition { it.debuff.isZero() && !it.hasNegativeStatus }
        return postCombatNoDebuffBestTarget(self, noDebuff) ?: withDebuffBestTarget(withDebuff)
    }

    private fun withDebuffBestTarget(
        targets: List<HeroUnit>
    ): HeroUnit? {
        return targets.maxBy { it.id }
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

    private fun postCombatNoDebuffBestTarget(
        self: HeroUnit,
        targets: List<HeroUnit>
    ): HeroUnit? {
        return targets.asSequence().map { target ->
            target to healAmount(self, target)
        }.bestHealTarget()
    }

}

object Restore: RestoreAssist() {
    override fun healAmount(self: HeroUnit, target: HeroUnit): Int {
        return healAmount(8, self, target)
    }
}

object RestorePlus: RestoreAssist() {
    override fun healAmount(self: HeroUnit, target: HeroUnit): Int {
        return healAmount(maxOf(8, self.visibleStat.atk / 2), self, target)
    }
}