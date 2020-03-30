package me.kumatheta.feh.skill.effect.teleport

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Position
import me.kumatheta.feh.skill.allies
import me.kumatheta.feh.skill.effect.TeleportEffect
import me.kumatheta.feh.util.surroundings

class WingsOfMercy(private val hpThreshold: Int) : TeleportEffect {
    override fun getTeleportLocations(battleState: BattleState, self: HeroUnit): Sequence<Position> {
        return self.allies(battleState).filter {
            it.hpThreshold(hpThreshold) <= 0
        }.flatMap {
            it.position.surroundings(battleState.maxPosition)
        }
    }
}