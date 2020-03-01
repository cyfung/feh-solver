package me.kumatheta.feh.skill.effect.startofturn

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat
import me.kumatheta.feh.foe
import me.kumatheta.feh.skill.BasicSkill

fun threaten(stat: Stat) = BasicSkill(
    startOfTurn = { battleState: BattleState, self: HeroUnit ->
        val position = self.position
        battleState.unitsSeq(self.team.foe).filter { it.position.distanceTo(position) <= 2 }.forEach {
            it.applyDebuff(stat)
        }
    }
)
