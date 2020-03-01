package me.kumatheta.feh.skill.effect.startofturn

import me.kumatheta.feh.*
import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.MapSkillMethod
import me.kumatheta.feh.skill.adjacentAllies
import me.kumatheta.feh.util.surroundings

val MOVE_ORDER_EFFECT: MapSkillMethod<Sequence<Position>> = { battleState, self ->
    battleState.unitsSeq(self.team).filter { it != self }.filter {
        it.position.distanceTo(self.position) <= 2
    }.flatMap {
        it.position.surroundings(battleState.maxPosition)
    }
}

val airOrder3 = BasicSkill(
    startOfTurn = { battleState: BattleState, self: HeroUnit ->
        self.adjacentAllies(battleState).filter {
            it.moveType == MoveType.FLYING
        }.forEach {
            it.addPositiveStatus(PositiveStatus.MOVEMENT_ORDER)
        }
    }
)