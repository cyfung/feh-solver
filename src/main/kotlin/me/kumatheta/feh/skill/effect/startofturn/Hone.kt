package me.kumatheta.feh.skill.effect.startofturn

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.adjacentAllies

fun hone(stat: Stat) = BasicSkill(
    startOfTurn = { battleState: BattleState, self: HeroUnit ->
        self.adjacentAllies(battleState).forEach {
            it.applyBuff(stat)
        }
    }
)
