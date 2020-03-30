package me.kumatheta.feh.skill.assist

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.CombatResult
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Position
import me.kumatheta.feh.Stat
import me.kumatheta.feh.util.compareByDescending

class RallyUp(private val bonus: Stat) : BuffRelatedAssist() {
    override fun apply(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState
    ) {
        battleState.unitsSeq(self.team).filterNot {
            it == self
        }.filter {
            it == target || it.position.distanceTo(target.position) <= 2
        }.forEach {
            it.applyBuff(bonus)
        }
    }

    override fun isValidAction(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState,
        fromPosition: Position
    ): Boolean {
        return target.extraBuffAmount(bonus) > 0
    }

    data class Target(val unit: HeroUnit, val helpedCount: Int, val reference: HeroUnit, val refBuffed: Int)

    private fun getBestTarget(
        targets: Set<HeroUnit>,
        potentialUnits: Sequence<HeroUnit>,
        distanceToClosestFoe: Map<HeroUnit, Int>
    ): HeroUnit? {
        val unitsConcerned = potentialUnits.map {
            it to it.extraBuffAmount(bonus)
        }.filter {
            it.second >= 2
        }
        return targets.asSequence().flatMap { target ->
            val helped = unitsConcerned.filter { it.first.position.distanceTo(target.position) <= 2 }.toList()
            val targetPair = helped.firstOrNull() { it.first == target }
            if (targetPair != null) {
                sequenceOf(targetPair)
            } else {
                helped.asSequence()
            }.map {
                Target(target, helped.size, it.first, it.second)
            }
        }.minWith(
            compareByDescending<Target>(
                { it.helpedCount },
                { it.refBuffed }
            ).thenBy {
                distanceToClosestFoe[it.reference]
            }.thenByDescending {
                it.reference.currentStatTotal
            }.thenByDescending {
                it.unit.id
            }
        )?.unit
    }

    override fun preCombatBestTarget(
        self: HeroUnit,
        targets: Set<HeroUnit>,
        possibleAttacks: Map<HeroUnit, List<CombatResult>>,
        lazyAllyThreat: Lazy<Map<HeroUnit, Set<HeroUnit>>>,
        distanceToClosestFoe: Map<HeroUnit, Int>
    ): HeroUnit? {
        val unitsConcerned = possibleAttacks.asSequence().filter { it.value.isNotEmpty() }.map { it.key }.filter {
            it != self
        }
        return getBestTarget(targets, unitsConcerned, distanceToClosestFoe)
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
        }).filterNot {
            it == self
        }.distinct()

        return getBestTarget(targets, unitsConcerned, distanceToClosestFoe)
    }
}

