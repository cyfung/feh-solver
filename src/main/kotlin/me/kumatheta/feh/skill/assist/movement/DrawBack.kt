package me.kumatheta.feh.skill.assist.movement

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Position
import me.kumatheta.feh.skill.MovementEffect
import me.kumatheta.feh.skill.MovementEffect.Companion.isValidAndUnoccupied
import me.kumatheta.feh.skill.MovementEffect.Companion.positionTransform
import me.kumatheta.feh.skill.ProtectiveMovementAssist

object DrawBack : ProtectiveMovementAssist(true, DrawBackEffect)

object DrawBackEffect : MovementEffect {
    override fun applyMovement(self: HeroUnit, target: HeroUnit, battleState: BattleState) {
        val startPosition = self.position
        val endPosition =
            selfEndPosition(startPosition, target.position)
        battleState.move(self, endPosition)
        battleState.move(target, startPosition)
    }

    override fun isValidAction(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState,
        fromPosition: Position
    ): Boolean {
        val selfEndPosition = SwapEffect.selfEndPosition(fromPosition, target.position)
        return selfEndPosition.isValidAndUnoccupied(battleState, self) && battleState.isValidPosition(
            target,
            fromPosition
        )
    }

    override fun targetEndPosition(
        battleState: BattleState,
        self: HeroUnit,
        selfPosition: Position,
        targetPosition: Position
    ): Position {
        return selfPosition
    }

    override fun selfEndPosition(selfPosition: Position, targetPosition: Position): Position {
        return positionTransform(selfPosition, targetPosition, -1)
    }
}