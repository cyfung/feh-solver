package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.MoveType
import me.kumatheta.feh.Position
import me.kumatheta.feh.skill.MapSkillMethod
import me.kumatheta.feh.skill.TeleportEffect
import me.kumatheta.feh.skill.nearbyAllies
import me.kumatheta.feh.util.surroundings

val Aerobatics3: TeleportEffect = { battleState, self ->
    self.nearbyAllies(battleState, 2).filter {
        when (it.moveType) {
            MoveType.INFANTRY, MoveType.CAVALRY, MoveType.ARMORED -> true
            else -> false
        }
    }.flatMap {
        it.position.surroundings(battleState.maxPosition)
    }
}
