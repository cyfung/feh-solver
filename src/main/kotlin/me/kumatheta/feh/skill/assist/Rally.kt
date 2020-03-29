package me.kumatheta.feh.skill.assist

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.CombatResult
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Position
import me.kumatheta.feh.Stat

class Rally(private val bonus: Stat) : BuffRelatedAssist() {
    override fun apply(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState
    ) {
        target.applyBuff(bonus)
    }

    override fun isValidAction(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState,
        fromPosition: Position
    ): Boolean {
        return target.extraBuffAmount(bonus) > 0
    }

    private fun getBestTarget(
        targets: Set<HeroUnit>,
        distanceToClosestFoe: Map<HeroUnit, Int>,
        unitsConcerned: Collection<HeroUnit>
    ): HeroUnit? {
        return targets.asSequence().filter {
            unitsConcerned.contains(it)
        }.map {
            it to it.extraBuffAmount(bonus)
        }.filter {
            it.second >= 2
        }.minWith(
            targetComparator(distanceToClosestFoe)
        )?.first
    }

    override fun preCombatBestTarget(
        self: HeroUnit,
        targets: Set<HeroUnit>,
        possibleAttacks: Map<HeroUnit, List<CombatResult>>,
        lazyAllyThreat: Lazy<Map<HeroUnit, Set<HeroUnit>>>,
        distanceToClosestFoe: Map<HeroUnit, Int>
    ): HeroUnit? {
        return getBestTarget(targets, distanceToClosestFoe, possibleAttacks.keys)
    }

    override fun postCombatBestTarget(
        self: HeroUnit,
        targets: Set<HeroUnit>,
        lazyAllyThreat: Lazy<Map<HeroUnit, Set<HeroUnit>>>,
        foeThreat: Map<Position, Int>,
        distanceToClosestFoe: Map<HeroUnit, Int>,
        battleState: BattleState
    ): HeroUnit? {
        val allyThreat = lazyAllyThreat.value.keys
        val unitsConcerned = (allyThreat.asSequence() + battleState.unitsSeq(self.team).filter {
            val threatened = foeThreat[it.position] ?: 0
            threatened > 0
        }).toSet()
        return getBestTarget(targets, distanceToClosestFoe, unitsConcerned)
    }
}

