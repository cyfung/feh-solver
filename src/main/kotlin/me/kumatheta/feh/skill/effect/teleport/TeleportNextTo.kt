package me.kumatheta.feh.skill.effect.teleport

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.MoveType
import me.kumatheta.feh.Position
import me.kumatheta.feh.skill.effect.TeleportEffect
import me.kumatheta.feh.skill.nearbyAllies
import me.kumatheta.feh.util.surroundings

class TeleportNextTo(private val moveTypes: Set<MoveType>) : TeleportEffect {
    override fun getTeleportLocations(battleState: BattleState, self: HeroUnit): Sequence<Position> {
        return self.nearbyAllies(battleState, 2).filter {
            moveTypes.contains(it.moveType)
        }.flatMap {
            it.position.surroundings(battleState.maxPosition)
        }
    }
}

val Aerobatics3 = TeleportNextTo(setOf(MoveType.INFANTRY, MoveType.ARMORED, MoveType.CAVALRY))
val FlierFormation3 = TeleportNextTo(setOf(MoveType.FLYING))