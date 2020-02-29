package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.MapSkillMethod
import me.kumatheta.feh.Passive

object TimePulse3 : Passive {
    override val startOfTurn: MapSkillMethod<Unit>? = { _: BattleState, self: HeroUnit ->
        if (self.cooldown == self.cooldownCount) {
            self.cachedEffect.cooldown--
        }
    }
}