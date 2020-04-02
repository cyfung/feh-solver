package me.kumatheta.feh.skill.effect.startofturn

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.MoveType
import me.kumatheta.feh.Position
import me.kumatheta.feh.PositiveStatus
import me.kumatheta.feh.skill.adjacentAllies
import me.kumatheta.feh.skill.effect.StartOfTurnEffect
import me.kumatheta.feh.skill.effect.TeleportEffect
import me.kumatheta.feh.util.surroundings

object MoveOrderEffect : TeleportEffect {
    override fun getTeleportLocations(battleState: BattleState, self: HeroUnit): Sequence<Position> {
        return battleState.unitsSeq(self.team).filter { it != self }.filter {
            it.position.distanceTo(self.position) <= 2
        }.flatMap {
            it.position.surroundings(battleState.maxPosition)
        }
    }
}

class Orders3(private val isFlying: Boolean) : StartOfTurnEffect {
    override fun onStartOfTurn(battleState: BattleState, self: HeroUnit) {
        self.adjacentAllies(battleState).filter {
            (it.moveType == MoveType.FLYING) == isFlying
        }.forEach {
            it.addPositiveStatus(PositiveStatus.MOVEMENT_ORDER)
        }
    }
}