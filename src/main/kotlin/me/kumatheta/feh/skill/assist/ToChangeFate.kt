package me.kumatheta.feh.skill.assist

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.NegativeStatus
import me.kumatheta.feh.Position
import me.kumatheta.feh.ProtectiveMovementAssist
import me.kumatheta.feh.Stat

private val BUFF = Stat(atk = 6)

object ToChangeFate : ProtectiveMovementAssist(true) {
    override fun targetEndPosition(battleState: BattleState, self: HeroUnit, selfPosition: Position, targetPosition: Position): Position {
        return Reposition.targetEndPosition(battleState, self, selfPosition, targetPosition)
    }

    override fun selfEndPosition(selfPosition: Position, targetPosition: Position): Position {
        return Reposition.selfEndPosition(selfPosition, targetPosition)
    }

    override fun apply(self: HeroUnit, target: HeroUnit, battleState: BattleState) {
        Reposition.apply(self, target, battleState)
        self.applyBuff(BUFF)
        self.addNegativeStatus(NegativeStatus.ISOLATION)
    }

    override fun isValidAction(self: HeroUnit, target: HeroUnit, battleState: BattleState, fromPosition: Position): Boolean {
        return Reposition.isValidAction(self, target, battleState, fromPosition)
    }

}