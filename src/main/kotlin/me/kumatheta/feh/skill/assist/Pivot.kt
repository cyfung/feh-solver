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
        val endPosition = selfEndPosition(fromPosition, target.position)
        return isValidPosition(battleState, self, endPosition)
    }

    override fun selfEndPosition(selfPosition: Position, targetPosition: Position): Position {
        return positionTransform(selfPosition, targetPosition, 2)
    }
}