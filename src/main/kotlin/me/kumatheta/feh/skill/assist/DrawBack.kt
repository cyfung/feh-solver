package me.kumatheta.feh.skill.assist

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.MovementAssist
import me.kumatheta.feh.Position

object DrawBack : MovementAssist(true) {
    override fun apply(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState
    ) {
        val startPosition = self.position
        val endPosition = selfEndPosition(startPosition, target.position)
        battleState.move(self, endPosition)
        battleState.move(target, startPosition)
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
                -1 -> Position(startPosition.x, startPosition.y - 1)
                1 -> Position(startPosition.x, startPosition.y + 1)
                else -> null
            }
            startPosition.y == assistTargetPosition.y -> when (startPosition.x - assistTargetPosition.x) {
                -1 -> Position(startPosition.x - 1, startPosition.y)
                1 -> Position(startPosition.x + 1, startPosition.y)
                else -> null
            }
            else -> null
        }

    }
}