package me.kumatheta.feh.skill.effect.startofturn

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.effect.StartOfTurnEffect

object TimePulse3: StartOfTurnEffect {
    override fun onStartOfTurn(battleState: BattleState, self: HeroUnit) {
        if (self.cooldown == self.cooldownCount) {
            self.cachedEffect.cooldown--
        }
    }
}
