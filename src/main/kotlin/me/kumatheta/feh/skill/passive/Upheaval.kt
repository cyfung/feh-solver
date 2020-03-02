package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.foe
import me.kumatheta.feh.skill.BasicSkill

val Upheaval = BasicSkill(
    startOfTurn = { battleState: BattleState, heroUnit: HeroUnit ->
        if (battleState.turn == 1) {
            battleState.unitsSeq(heroUnit.team.foe).forEach {
                it.takeNonLethalDamage(7)
            }
        }
    }
)