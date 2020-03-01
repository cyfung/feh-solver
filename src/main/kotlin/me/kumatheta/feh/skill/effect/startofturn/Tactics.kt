package me.kumatheta.feh.skill.effect.startofturn

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.BasicSkill

fun tactics(stat: Stat) = BasicSkill(
    startOfTurn = { battleState: BattleState, self: HeroUnit ->
        val pos = self.position
        val team = battleState.unitsSeq(self.team).toList()
        val counts = team.asSequence().groupingBy { it.moveType }.eachCount()
        team.filterNot { it == self }.filter { counts[it.moveType] ?: 0 <= 2 }.filter {
            it.position.distanceTo(pos) <= 2
        }.forEach {
            it.applyBuff(stat)
        }
    }
)