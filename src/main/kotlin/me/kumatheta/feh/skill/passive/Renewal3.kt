package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.MapSkillMethod

fun renewal(turn: Int): MapSkillMethod<Unit> = { battleState: BattleState, self: HeroUnit ->
    if (battleState.turn % turn == 1) {
        self.heal(10)
    }
}
