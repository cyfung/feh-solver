package me.kumatheta.feh

import me.kumatheta.feh.skill.assist.Pivot

sealed class Assist : Skill {
    abstract fun apply(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState
    )
    abstract fun isValidAction(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState,
        fromPosition: Position
    ): Boolean
}
abstract class MovementAssist(val canBeAggressive: Boolean) : Assist() {
    abstract fun endPosition(startPosition: Position, assistTargetPosition: Position): Position
}

abstract class NormalAssist : Assist() {
    abstract fun isValidPreCombat(
        self: HeroUnit,
        selfAttacks: List<CombatResult>
    ): Boolean

    abstract fun preCombatBestTarget(
        self: HeroUnit,
        targets: Set<HeroUnit>,
        lazyAllyThreat: Lazy<Map<HeroUnit, Set<HeroUnit>>>,
        distanceToClosestFoe: Map<HeroUnit, Int>
    ): HeroUnit?

    abstract fun postCombatBestTarget(
        self: HeroUnit,
        targets: Set<HeroUnit>,
        lazyAllyThreat: Lazy<Map<HeroUnit, Set<HeroUnit>>>,
        foeThreat: Map<Position, Int>,
        distanceToClosestFoe: Map<HeroUnit, Int>,
        battleState: BattleState
    ): HeroUnit?
}