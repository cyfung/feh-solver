package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.MapSkillMethod
import me.kumatheta.feh.skill.Passive

object Renewal3 : BasicSkill() {
    override val startOfTurn: MapSkillMethod<Unit>? = { battleState: BattleState, self: HeroUnit ->
        if (battleState.turn % 2 == 1) {
            self.heal(10)
        }
    }
}