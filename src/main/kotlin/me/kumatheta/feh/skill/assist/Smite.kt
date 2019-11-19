package me.kumatheta.feh.skill.assist

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.MovementAssist
import me.kumatheta.feh.Position

object Smite : MovementAssist(true) {
    override fun apply(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState
    ) {
        val startPosition = self.position
        val endPosition = checkEndPosition(startPosition, target.position) ?: throw IllegalArgumentException()
        return if (isValid(battleState, self, target, endPosition)) {
            battleState.move(target, endPosition)
        } else {
            val position = checkEndPosition2(startPosition, target.position) ?: throw IllegalArgumentException()
            battleState.move(target, position)
        }
    }

    override fun isValidAction(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState,
        fromPosition: Position
    ): Boolean {
        val endPosition = checkEndPosition(fromPosition, target.position) ?: throw IllegalArgumentException()
        return if (isValid(battleState, self, target, endPosition)) {
            true
        } else {
            isValid(
                battleState,
                self,
                target,
                checkEndPosition2(fromPosition, target.position) ?: throw IllegalArgumentException()
            )
        }
    }

    private fun isValid(
        battleState: BattleState,
        self: HeroUnit,
        target: HeroUnit,
        endPosition: Position
    ): Boolean {
        if (!battleState.isValidPosition(target, endPosition)) return false
        val chessPiece = battleState.getChessPiece(endPosition)
        return chessPiece == null || chessPiece == self
    }

    override fun selfEndPosition(startPosition: Position, assistTargetPosition: Position): Position {
        return startPosition
    }

    private fun checkEndPosition(startPosition: Position, assistTargetPosition: Position): Position? {
        return when {
            startPosition.x == assistTargetPosition.x -> when (startPosition.y - assistTargetPosition.y) {
                -1 -> Position(startPosition.x, startPosition.y + 3)
                1 -> Position(startPosition.x, startPosition.y - 3)
                else -> null
            }
            startPosition.y == assistTargetPosition.y -> when (startPosition.x - assistTargetPosition.x) {
                -1 -> Position(startPosition.x + 3, startPosition.y)
                1 -> Position(startPosition.x - 3, startPosition.y)
                else -> null
            }
            else -> null
        }
    }

    private fun checkEndPosition2(startPosition: Position, assistTargetPosition: Position): Position? {
        return when {
            startPosition.x == assistTargetPosition.x -> when (startPosition.y - assistTargetPosition.y) {
                -1 -> Position(startPosition.x, startPosition.y + 2)
                1 -> Position(startPosition.x, startPosition.y - 2)
                else -> null
            }
            startPosition.y == assistTargetPosition.y -> when (startPosition.x - assistTargetPosition.x) {
                -1 -> Position(startPosition.x + 2, startPosition.y)
                1 -> Position(startPosition.x - 2, startPosition.y)
                else -> null
            }
            else -> null
        }
    }
}