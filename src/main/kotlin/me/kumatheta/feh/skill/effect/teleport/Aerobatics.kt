package me.kumatheta.feh.skill.effect.teleport

import me.kumatheta.feh.MoveType
import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.nearbyAllies
import me.kumatheta.feh.util.surroundings

val Aerobatics3 = BasicSkill(
    teleport = { battleState, self ->
        self.nearbyAllies(battleState, 2).filter {
            when (it.moveType) {
                MoveType.INFANTRY, MoveType.CAVALRY, MoveType.ARMORED -> true
                else -> false
            }
        }.flatMap {
            it.position.surroundings(battleState.maxPosition)
        }
    }
)