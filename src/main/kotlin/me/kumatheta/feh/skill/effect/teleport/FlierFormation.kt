package me.kumatheta.feh.skill.effect.teleport

import me.kumatheta.feh.MoveType
import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.nearbyAllies
import me.kumatheta.feh.util.surroundings

val FlierFormation3 = BasicSkill(
    teleport = { battleState, self ->
        self.nearbyAllies(battleState, 2).filter {
            it.moveType == MoveType.FLYING
        }.flatMap {
            it.position.surroundings(battleState.maxPosition)
        }
    }
)
