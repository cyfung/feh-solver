package me.kumatheta.feh.skill.assist.movement

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Position
import me.kumatheta.feh.skill.MovementAssist
import me.kumatheta.feh.skill.MovementEffect
import me.kumatheta.feh.skill.MovementEffect.Companion.isValidPosition
import me.kumatheta.feh.skill.MovementEffect.Companion.positionTransform

object Pivot: MovementAssist(false, PivotEffect)

object PivotEffect : MovementEffect {
    override fun applyMovement(self: HeroUnit, target: HeroUnit, battleState: BattleState) {
        val endPosition =
            selfEndPosition(self.position, target.position)
        battleState.move(self, endPosition)
    }

    override fun isValidAction(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState,
        fromPosition: Position
    ): Boolean {
        val endPosition =
            selfEndPosition(fromPosition, target.position)
        return isValidPosition(battleState, self, endPosition)
    }

    override fun selfEndPosition(selfPosition: Position, targetPosition: Position): Position {
        return positionTransform(selfPosition, targetPosition, 2)
    }

    override fun targetEndPosition(
        battleState: BattleState,
        self: HeroUnit,
        selfPosition: Position,
        targetPosition: Position
    ): Position {
        return targetPosition
    }
}