package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.MapSkillMethod
import me.kumatheta.feh.Passive
import me.kumatheta.feh.Position
import me.kumatheta.feh.util.surroundings
import me.kumatheta.feh.MoveType
import me.kumatheta.feh.skill.nearbyAllies

object FlierFormation3 : Passive {
    override val teleport: MapSkillMethod<Sequence<Position>>? = { battleState, self ->
        self.nearbyAllies(battleState, 2).filter {
            it.moveType == MoveType.FLYING
        }.flatMap {
            it.position.surroundings(battleState.maxPosition)
        }
    }

}