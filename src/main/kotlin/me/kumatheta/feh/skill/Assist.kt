package me.kumatheta.feh.skill

import me.kumatheta.feh.*

sealed class Assist(val isRange: Boolean) : Skill {
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

abstract class MovementAssist(val canBeAggressive: Boolean, val movementEffect: MovementEffect) :
    Assist(false) {
    final fun selfEndPosition(selfPosition: Position, targetPosition: Position): Position
        = movementEffect.selfEndPosition(selfPosition, targetPosition)

    open fun onMove(self: HeroUnit, target: HeroUnit, battleState: BattleState) {
    }

    final override fun apply(self: HeroUnit, target: HeroUnit, battleState: BattleState) {
        movementEffect.applyMovement(self, target, battleState)
        onMove(self, target, battleState)
    }

    final override fun isValidAction(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState,
        fromPosition: Position
    ): Boolean {
        return movementEffect.isValidAction(self, target, battleState, fromPosition)
    }
}

abstract class ProtectiveMovementAssist(canBeAggressive: Boolean, movementEffect: MovementEffect) :
    MovementAssist(canBeAggressive, movementEffect) {
    fun targetEndPosition(
        battleState: BattleState,
        self: HeroUnit,
        selfPosition: Position,
        targetPosition: Position
    ): Position {
        return movementEffect.targetEndPosition(battleState, self, selfPosition, targetPosition)
    }
}

abstract class NormalAssist(isRange: Boolean = false) : Assist(isRange) {
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