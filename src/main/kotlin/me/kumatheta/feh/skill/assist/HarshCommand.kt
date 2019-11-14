package me.kumatheta.feh.skill.assist

import me.kumatheta.feh.HeroUnit

object HarshCommand : BuffRelatedAssist() {
    override fun apply(self: HeroUnit, target: HeroUnit) {
        target.applyBuff(-target.debuff)
        target.clearPenalty()
    }

    override fun isValidAction(self: HeroUnit, target: HeroUnit): Boolean {
        return target.debuff.isNotZero()
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
            it to (it.stat.rallyGain(-it.debuff) + it.debuff.totalExceptHp)
        }.filter {
            it.second >= 2
        }.minWith(
            targetComparator(distanceToClosestEnemy)
        )?.first
    }
}