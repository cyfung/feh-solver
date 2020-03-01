package me.kumatheta.feh.skill.effect.startofturn

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.allies

fun infantryPulse(minDiff: Int) = BasicSkill(
    startOfTurn = { battleState: BattleState, self: HeroUnit ->
        if (battleState.turn == 1) {
            self.allies(battleState).forEach {
                if (it.currentHp <= self.currentHp - minDiff) {
                    it.cachedEffect.cooldown--
                }
            }
        }
    }
)