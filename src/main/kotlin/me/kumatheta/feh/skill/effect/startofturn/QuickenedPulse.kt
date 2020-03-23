package me.kumatheta.feh.skill.effect.startofturn

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.effect.StartOfTurnEffect

object QuickenedPulse: StartOfTurnEffect {
    override fun onStartOfTurn(battleState: BattleState, self: HeroUnit) {
        if (battleState.turn == 1) {
            self.cachedEffect.cooldown--
        }
    }
}
