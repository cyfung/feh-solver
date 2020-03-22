package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit

interface GuidanceEffect {
    fun canTeleportNextTo(battleState: BattleState, self: HeroUnit, target: HeroUnit): Boolean
}