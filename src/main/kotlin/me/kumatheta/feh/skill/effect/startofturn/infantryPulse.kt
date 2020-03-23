package me.kumatheta.feh.skill.effect.startofturn

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.allies
import me.kumatheta.feh.skill.effect.StartOfTurnEffect

class InfantryPulse(private val minDiff: Int): StartOfTurnEffect {
    override fun onStartOfTurn(battleState: BattleState, self: HeroUnit) {
        if (battleState.turn == 1) {
            self.allies(battleState).forEach {
                if (it.currentHp <= self.currentHp - minDiff) {
                    it.cachedEffect.cooldown--
                }
            }
        }
    }
}
