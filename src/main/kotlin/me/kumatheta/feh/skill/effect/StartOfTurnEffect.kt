package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit

interface StartOfTurnEffect {
    fun onStartOfTurn(battleState: BattleState, self: HeroUnit)
}