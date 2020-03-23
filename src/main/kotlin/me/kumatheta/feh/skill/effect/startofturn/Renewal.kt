package me.kumatheta.feh.skill.effect.startofturn

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.effect.StartOfTurnEffect

class Renewal(private val turn: Int) : StartOfTurnEffect {
    override fun onStartOfTurn(battleState: BattleState, self: HeroUnit) {
        if (battleState.turn % turn == 1) {
            self.heal(10)
        }
    }
}