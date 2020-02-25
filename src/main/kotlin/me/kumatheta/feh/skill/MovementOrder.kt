package me.kumatheta.feh.skill

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.MapSkillMethod
import me.kumatheta.feh.MoveType
import me.kumatheta.feh.Position
import me.kumatheta.feh.PositiveStatus
import me.kumatheta.feh.util.surroundings

val MOVE_ORDER_EFFECT: MapSkillMethod<Sequence<Position>> = { battleState, self ->
    battleState.unitsSeq(self.team).filter { it != self }.filter {
        it.position.distanceTo(self.position) <= 2
    }.flatMap {
        it.position.surroundings(battleState.maxPosition)
    }
}

val airOrder3: MapSkillMethod<Unit> = { battleState: BattleState, self: HeroUnit ->
    self.adjacentAllies(battleState).filter {
        it.moveType == MoveType.FLYING
    }.forEach {
        it.addPositiveStatus(PositiveStatus.MOVEMENT_ORDER)
    }
}