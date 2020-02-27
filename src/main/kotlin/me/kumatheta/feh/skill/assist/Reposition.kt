package me.kumatheta.feh.skill.assist

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Position
import me.kumatheta.feh.ProtectiveMovementAssist

object Reposition : ProtectiveMovementAssist(true) {
    override fun apply(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState
    ) {
        val targetEndPosition = targetEndPosition(battleState, self, self.position, target.position)
        battleState.move(target, targetEndPosition)
    }

    override fun isValidAction(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState,
        fromPosition: Position
    ): Boolean {
        val targetEndPosition = targetEndPosition(battleState, self, fromPosition, target.position)
        return isValidPosition(battleState, self, targetEndPosition)
    }

    override fun targetEndPosition(
        battleState: BattleState,
        self: HeroUnit,
        selfPosition: Position,
        targetPosition: Position
    ): Position {
        return positionTransform(selfPosition, targetPosition, -1)
    }

    override fun selfEndPosition(selfPosition: Position, targetPosition: Position): Position {
        return selfPosition
    }
}