package me.kumatheta.feh.skill.assist

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.CombatResult
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Position
import me.kumatheta.feh.skill.BASE_ASSIST_COMPARATOR
import me.kumatheta.feh.skill.NormalAssist

open class Refresh : NormalAssist() {
    override fun apply(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState
    ) {
        target.refresh()
    }

    override fun isValidAction(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState,
        fromPosition: Position
    ): Boolean {
        return !target.available && target.assist !is Refresh
    }

    final override fun isValidPreCombat(
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
        lazyAllyThreat: Lazy<Map<HeroUnit, Set<HeroUnit>>>,
        distanceToClosestFoe: Map<HeroUnit, Int>
    ): HeroUnit? {
        return targets.filterNot {
            it.available
        }.minWith(
            compareBy<HeroUnit> {
                if (lazyAllyThreat.value.contains(it)) {
                    0
                } else {
                    1
                }
            }.thenBy(BASE_ASSIST_COMPARATOR) {
                it
            }
        )
    }

    override fun postCombatBestTarget(
        self: HeroUnit,
        targets: Set<HeroUnit>,
        lazyAllyThreat: Lazy<Map<HeroUnit, Set<HeroUnit>>>,
        foeThreat: Map<Position, Int>,
        distanceToClosestFoe: Map<HeroUnit, Int>,
        battleState: BattleState
    ): HeroUnit? {
        throw UnsupportedOperationException()
    }
}