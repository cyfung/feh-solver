package me.kumatheta.feh.skill.effect.startofturn

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.BasicSkill

val TimePulse3 = BasicSkill(
    startOfTurn = { _: BattleState, self: HeroUnit ->
        if (self.cooldown == self.cooldownCount) {
            self.cachedEffect.cooldown--
        }
    }
)
