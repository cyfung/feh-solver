package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.skill.MapSkillMethod
import me.kumatheta.feh.MoveType
import me.kumatheta.feh.skill.Passive
import me.kumatheta.feh.Position
import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.nearbyAllies
import me.kumatheta.feh.util.surroundings

object FlierFormation3 : BasicSkill() {
    override val teleport: MapSkillMethod<Sequence<Position>>? = { battleState, self ->
        self.nearbyAllies(battleState, 2).filter {
            it.moveType == MoveType.FLYING
        }.flatMap {
            it.position.surroundings(battleState.maxPosition)
        }
    }

}