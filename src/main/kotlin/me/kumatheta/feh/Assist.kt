package me.kumatheta.feh

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

abstract class MovementAssist(val canBeAggressive: Boolean) : Assist(false) {
    abstract fun selfEndPosition(selfPosition: Position, targetPosition: Position): Position

    fun isValidPosition(battleState: BattleState, self: HeroUnit, position: Position): Boolean {
        if (!battleState.isValidPosition(self, position)) return false
        val chessPiece = battleState.getChessPiece(position)
        return chessPiece == null || chessPiece == self
    }

    fun positionTransform(a: Position, b: Position, change: Int): Position {
        return when {
            a.x == b.x -> when (a.y - b.y) {
                -1 -> Position(a.x, a.y + change)
                1 -> Position(a.x, a.y - change)
                else -> throw IllegalStateException()
            }
            a.y == b.y -> when (a.x - b.x) {
                -1 -> Position(a.x + change, a.y)
                1 -> Position(a.x - change, a.y)
                else -> throw IllegalStateException()
            }
            else -> throw IllegalStateException()
        }
    }
}

abstract class ProtectiveMovementAssist(canBeAggressive: Boolean) : MovementAssist(canBeAggressive) {
    abstract fun targetEndPosition(
        battleState: BattleState,
        self: HeroUnit,
        selfPosition: Position,
        targetPosition: Position
    ): Position
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