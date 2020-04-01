package me.kumatheta.feh.skill.effect.startofturn

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.foe
import me.kumatheta.feh.skill.effect.StartOfTurnEffect

class PulseTie(private val minDiff: Int, private val evenNumberedTurn: Boolean) : StartOfTurnEffect {
    override fun onStartOfTurn(battleState: BattleState, self: HeroUnit) {
        val isEvenNumbered = battleState.turn % 2 == 0
        if (isEvenNumbered == evenNumberedTurn) {
            val cachedEffect = battleState.unitsSeq(self.team.foe).filter {
                it.cooldown == 0
            }.filter {
                it.currentHp + minDiff <= self.currentHp
            }.minBy {
                it.currentHp
            }?.cachedEffect ?: return
            cachedEffect.cooldown = cachedEffect.cooldown + 2
        }
    }
}
