package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.MapSkillMethod
import me.kumatheta.feh.skill.Passive

object TimePulse3 : BasicSkill() {
    override val startOfTurn: MapSkillMethod<Unit>? = { _: BattleState, self: HeroUnit ->
        if (self.cooldown == self.cooldownCount) {
            self.cachedEffect.cooldown--
        }
    }
}