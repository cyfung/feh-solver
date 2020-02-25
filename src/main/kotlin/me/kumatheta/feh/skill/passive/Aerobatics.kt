package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.MapSkillMethod
import me.kumatheta.feh.Passive
import me.kumatheta.feh.Position
import me.kumatheta.feh.util.surroundings
import me.kumatheta.feh.MoveType

object Aerobatics3 : Passive {
    override val teleport: MapSkillMethod<Sequence<Position>>? = { battleState, self ->
        battleState.unitsSeq(self.team).filter { it != self }.filter {
            it.position.distanceTo(self.position) <= 2
        }.filter {
            when (it.moveType) {
                MoveType.INFANTRY, MoveType.CAVALRY, MoveType.ARMORED -> true
                else -> false
            }
        }.flatMap {
            it.position.surroundings(battleState.maxPosition)
        }
    }
}

