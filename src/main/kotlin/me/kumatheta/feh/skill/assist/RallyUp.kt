package me.kumatheta.feh.skill.assist

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Position
import me.kumatheta.feh.Stat

class RallyUp(private val bonus: Stat) : BuffRelatedAssist() {
    final override fun apply(
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

    final override fun isValidAction(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState,
        fromPosition: Position
    ): Boolean {
        return target.extraBuffAmount(bonus) > 0
    }

    final override fun preCombatBestTarget(
        self: HeroUnit,
        targets: Set<HeroUnit>,
        lazyAllyThreat: Lazy<Map<HeroUnit, Set<HeroUnit>>>,
        distanceToClosestFoe: Map<HeroUnit, Int>
    ): HeroUnit? {
        val allyThreat = lazyAllyThreat.value
        return targets.asSequence().map {
            it to allyThreat.keys.count { ally -> ally.position.distanceTo(it.position) <= 2 }
        }.filter {
            it.second > 0
        }.minWith(
            targetComparator(distanceToClosestFoe)
        )?.first
    }

    override fun postCombatBestTarget(
        self: HeroUnit,
        targets: Set<HeroUnit>,
        lazyAllyThreat: Lazy<Map<HeroUnit, Set<HeroUnit>>>,
        foeThreat: Map<Position, Int>,
        distanceToClosestFoe: Map<HeroUnit, Int>,
        battleState: BattleState
    ): HeroUnit? {
        val allyThreat = lazyAllyThreat.value.keys.filter {
            it.extraBuffAmount(bonus) >= 2
        }
        val threatOrThreatened = (allyThreat.asSequence() + battleState.unitsSeq(self.team).filterNot {
            it == self
        }.filter {
            val threatened = foeThreat[it.position] ?: 0
            threatened > 0
        }).toSet()

        return targets.asSequence().map {
            it to threatOrThreatened.count { ally -> ally.position.distanceTo(it.position) <= 2 }
        }.filter {
            it.second > 0
        }.minWith(
            targetComparator(distanceToClosestFoe)
        )?.first
    }
}

