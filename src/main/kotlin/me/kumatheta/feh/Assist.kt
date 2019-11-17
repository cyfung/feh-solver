package me.kumatheta.feh

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
abstract class MovementAssist : Assist()

abstract class NormalAssist : Assist() {
    abstract fun isValidPreCombat(
        self: HeroUnit,
        selfAttacks: List<CombatResult>
    ): Boolean

    abstract fun preCombatBestTarget(
        self: HeroUnit,
        targets: Set<HeroUnit>,
        lazyAllyThreat: Lazy<Set<HeroUnit>>,
        distanceToClosestEnemy: Map<HeroUnit, Int>
    ): HeroUnit?
}