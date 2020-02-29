package me.kumatheta.feh.skill.assist.movement

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Position
import me.kumatheta.feh.skill.MovementEffect
import me.kumatheta.feh.skill.MovementEffect.Companion.isValidAndUnoccupied
import me.kumatheta.feh.skill.ProtectiveMovementAssist

object Swap : ProtectiveMovementAssist(false, SwapEffect)

object SwapEffect : MovementEffect {
    override fun applyMovement(self: HeroUnit, target: HeroUnit, battleState: BattleState) {
        if (target.isDead) {
            // Lunge
            battleState.move(self, target.position)
        } else {
            battleState.swap(self, target)
        }
    }

    override fun isValidAction(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState,
        fromPosition: Position
    ): Boolean {
        val selfEndPosition = target.position
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
        return targetPosition
    }
}