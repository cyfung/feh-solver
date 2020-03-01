package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.MapSkillMethod
import me.kumatheta.feh.skill.allies

fun infantryPulse(minDiff: Int): MapSkillMethod<Unit> = { battleState: BattleState, self: HeroUnit ->
    if (battleState.turn == 1) {
        self.allies(battleState).forEach {
            if (it.currentHp <= self.currentHp - minDiff) {
                it.cachedEffect.cooldown--
            }
        }
    }
}