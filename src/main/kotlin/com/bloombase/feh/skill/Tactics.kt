package com.bloombase.feh.skill

import com.bloombase.feh.BattleState
import com.bloombase.feh.HeroUnit
import com.bloombase.feh.MapSkillMethod
import com.bloombase.feh.Stat
import java.lang.IllegalStateException

class Tactics(private val stat: Stat) : MapSkillMethod<Unit> {

    override fun apply(battleState: BattleState, self: HeroUnit) {
        val pos = battleState.reverseMap[self]?: throw IllegalStateException()
        val team = battleState.unitsAndPos(self.team).toList()
        val counts = team.asSequence().map { it.key }.groupingBy { it.moveType }.eachCount()
        team.filterNot { it.key == self }.filter { counts[it.key.moveType] ?: 0 <= 2 }.filter {
            it.value.distanceTo(pos) <= 2
        }.forEach {
            it.key.applyBuff(stat)
        }
    }
}