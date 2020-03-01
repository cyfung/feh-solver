package me.kumatheta.feh.skill.effect.startofturn

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.adjacentAllies

fun wave(stat: Stat, oddTurn: Boolean) = BasicSkill(
    startOfTurn = { battleState: BattleState, self: HeroUnit ->
        val currentTurnOdd = battleState.turn % 2 == 1
        if (oddTurn == currentTurnOdd) {
            (self.adjacentAllies(battleState) + self).forEach {
                it.applyBuff(stat)
            }
        }
    }
)