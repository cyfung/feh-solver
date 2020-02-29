package me.kumatheta.feh.skill

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Passive
import me.kumatheta.feh.Position

interface MovementEffect {
    companion object {
        fun Position.isValidAndUnoccupied(
            battleState: BattleState,
            self: HeroUnit
        ): Boolean {
            if (!battleState.isValidPosition(self, this)) return false
            val chessPiece = battleState.getChessPiece(this)
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

    fun applyMovement(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState
    )

    fun isValidAction(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState,
        fromPosition: Position
    ): Boolean

    fun selfEndPosition(selfPosition: Position, targetPosition: Position): Position

    fun targetEndPosition(
        battleState: BattleState,
        self: HeroUnit,
        selfPosition: Position,
        targetPosition: Position
    ): Position
}

class MovementPassive(override val postInitiateMovement: MovementEffect) : Passive