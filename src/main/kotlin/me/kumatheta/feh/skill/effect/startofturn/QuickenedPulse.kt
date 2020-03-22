package me.kumatheta.feh.skill.effect.startofturn

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.BasicSkill

val QuickenedPulse = BasicSkill(
    startOfTurn = { battleState: BattleState, self: HeroUnit ->
        if (battleState.turn == 1) {
            self.cachedEffect.cooldown--
        }
    }
)
