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
        val endPosition = selfEndPosition(fromPosition, target.position)
        return Pivot.isValidPosition(battleState, self, endPosition)
    }

    override fun selfEndPosition(selfPosition: Position, targetPosition: Position): Position {
        return positionTransform(selfPosition, targetPosition, -1)
    }
}