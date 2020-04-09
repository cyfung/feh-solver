package me.kumatheta.feh.skill.effect.startofturn

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.PositiveStatus
import me.kumatheta.feh.skill.effect.StartOfTurnEffect

object ArmoredBoots : StartOfTurnEffect {
    override fun onStartOfTurn(battleState: BattleState, self: HeroUnit) {
        if (self.currentHp == self.maxHp) {
            self.addPositiveStatus(PositiveStatus.EXTRA_TRAVEL_POWER)
        }
    }
}