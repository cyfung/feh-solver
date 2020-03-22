package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit

interface ObstructEffect {
    fun canObstruct(battleState: BattleState, self: HeroUnit): Boolean
}