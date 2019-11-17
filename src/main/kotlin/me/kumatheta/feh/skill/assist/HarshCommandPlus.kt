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
        lazyAllyThreat: Lazy<Set<HeroUnit>>,
        distanceToClosestEnemy: Map<HeroUnit, Int>
    ): HeroUnit? {
        val allyThreat = lazyAllyThreat.value
        return targets.asSequence().filter { it.available }.filter {
            allyThreat.contains(it)
        }.map {
            if (it.withPanic) {
                it.stat.rallyGain(-it.debuff) + it.debuff.totalExceptHp + it.buff.totalExceptHp
            } else {
                it.stat.rallyGain(-it.debuff) + it.debuff.totalExceptHp
            }
            it to (it.stat.rallyGain(-it.debuff) + it.debuff.totalExceptHp)
        }.filter {
            it.second >= 2 || it.first.hasNegativeStatus
        }.minWith(
            targetComparator(distanceToClosestEnemy)
        )?.first
    }
}