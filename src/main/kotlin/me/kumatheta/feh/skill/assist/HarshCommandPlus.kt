package me.kumatheta.feh.skill.assist

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Position

object HarshCommandPlus : BuffRelatedAssist() {
    override fun apply(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState
    ) {
        target.applyBuff(-target.debuff)
        target.clearPenalty()
        target.clearNegativeStatus()
    }

    override fun isValidAction(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState,
        fromPosition: Position
    ): Boolean {
        return target.debuff.isNotZero() || target.hasNegativeStatus
    }

    override fun preCombatBestTarget(
        self: HeroUnit,
        targets: Set<HeroUnit>,
        lazyAllyThreat: Lazy<Map<HeroUnit, Set<HeroUnit>>>,
        distanceToClosestFoe: Map<HeroUnit, Int>
    ): HeroUnit? {
        val allyThreat = lazyAllyThreat.value
        return targets.asSequence().filter { it.available }.filter {
            allyThreat.contains(it)
        }.bestTarget(distanceToClosestFoe)
    }

    override fun postCombatBestTarget(
        self: HeroUnit,
        targets: Set<HeroUnit>,
        lazyAllyThreat: Lazy<Map<HeroUnit, Set<HeroUnit>>>,
        foeThreat: Map<Position, Int>,
        distanceToClosestFoe: Map<HeroUnit, Int>,
        battleState: BattleState
    ): HeroUnit? {
        val allyThreat = lazyAllyThreat.value
        return targets.asSequence().filter {
            allyThreat.contains(it) || (foeThreat[it.position] ?: 0) > 0
        }.bestTarget(distanceToClosestFoe)
    }

    private fun Sequence<HeroUnit>.bestTarget(distanceToClosestFoe: Map<HeroUnit, Int>): HeroUnit? {
        return map {
            it to if (it.withPanic) {
                it.buff.rallyGain(-it.debuff) + it.debuff.totalExceptHp + it.buff.totalExceptHp
            } else {
                it.buff.rallyGain(-it.debuff) + it.debuff.totalExceptHp
            }
        }.filter {
            it.second >= 2 || it.first.hasNegativeStatus
        }.minWith(
            targetComparator(distanceToClosestFoe)
        )?.first
    }
}