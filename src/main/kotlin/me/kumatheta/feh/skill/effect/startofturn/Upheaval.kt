package me.kumatheta.feh.skill.effect.startofturn

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.foe
import me.kumatheta.feh.skill.effect.StartOfTurnEffect

object Upheaval : StartOfTurnEffect {
    override fun onStartOfTurn(battleState: BattleState, self: HeroUnit) {
        if (battleState.turn == 1) {
            battleState.unitsSeq(self.team.foe).forEach {
                it.cachedEffect.takeNonLethalDamage(7)
            }
        }
    }
}