package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Position

interface TeleportEffect {
    fun getTeleportLocations(battleState: BattleState, self: HeroUnit): Sequence<Position>
}