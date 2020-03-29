package me.kumatheta.feh.skill.assist

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.CombatResult
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Position
import me.kumatheta.feh.Stat

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

    private fun getBestTarget(
        targets: Set<HeroUnit>,
        unitsConcerned: Sequence<HeroUnit>,
        distanceToClosestFoe: Map<HeroUnit, Int>
    ): HeroUnit? {
        return targets.asSequence().mapNotNull { target ->
            val helped = unitsConcerned.filter { ally -> ally.position.distanceTo(target.position) <= 2 }.toList()
            val reference = if (helped.contains(target)) {
                target
            } else {
                helped.asSequence().minWith(
                    compareBy({
                        distanceToClosestFoe[it]
                    }, {
                        it.currentStatTotal
                    })
                ) ?: return@mapNotNull null
            }
            // helped should no longer be empty
            Triple(target, helped.size, reference)
        }.minWith(
            compareByDescending<Triple<HeroUnit, Int, HeroUnit>> { it.second }.thenBy {
                distanceToClosestFoe[it.third]
            }.thenByDescending {
                it.third.currentStatTotal
            }.thenByDescending {
                it.first.id
            }
        )?.first
    }

    override fun preCombatBestTarget(
        self: HeroUnit,
        targets: Set<HeroUnit>,
        possibleAttacks: Map<HeroUnit, List<CombatResult>>,
        lazyAllyThreat: Lazy<Map<HeroUnit, Set<HeroUnit>>>,
        distanceToClosestFoe: Map<HeroUnit, Int>
    ): HeroUnit? {
        val unitsConcerned = possibleAttacks.keys.asSequence().filter {
            it != self && it.extraBuffAmount(bonus) >= 2
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
        }.filter {
            it.extraBuffAmount(bonus) >= 2
        }.distinct()

        return getBestTarget(targets, unitsConcerned, distanceToClosestFoe)
    }
}

