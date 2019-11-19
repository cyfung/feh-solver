package me.kumatheta.feh.skill.assist

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.MovementAssist
import me.kumatheta.feh.Position

object Pivot : MovementAssist(false) {
    override fun apply(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState
    ) {
        val endPosition = selfEndPosition(self.position, target.position)
        battleState.move(self, endPosition)
    }

    override fun isValidAction(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState,
        fromPosition: Position
    ): Boolean {
        val endPosition = checkEndPosition(fromPosition, target.position) ?: throw IllegalArgumentException()
        if (!battleState.isValidPosition(self, endPosition)) return false
        val chessPiece = battleState.getChessPiece(endPosition)
        return chessPiece == null || chessPiece == self
    }

    override fun selfEndPosition(startPosition: Position, assistTargetPosition: Position): Position {
        return checkEndPosition(startPosition, assistTargetPosition) ?: throw IllegalArgumentException()
    }

    private fun checkEndPosition(startPosition: Position, assistTargetPosition: Position): Position? {
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