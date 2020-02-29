package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.MapSkillMethod
import me.kumatheta.feh.Passive

object Renewal3 : Passive {
    override val startOfTurn: MapSkillMethod<Unit>? = { battleState: BattleState, self: HeroUnit ->
        if (battleState.turn % 2 == 1) {
            self.heal(10)
        }
    }
}