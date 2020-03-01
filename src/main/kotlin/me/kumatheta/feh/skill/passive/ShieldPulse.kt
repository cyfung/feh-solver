package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.BasicSkill

val ShieldPulse3 = BasicSkill(
    startOfTurn = { battleState: BattleState, self: HeroUnit ->
        if (battleState.turn == 1) {
            self.reduceCooldown(2)
        }
    },
    flatDamageReduce = { _, specialTriggered ->
        if (specialTriggered) {
            5
        } else {
            0
        }
    }
)
