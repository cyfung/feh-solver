package me.kumatheta.feh.skill

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.MapSkillMethod
import me.kumatheta.feh.Stat
import java.lang.IllegalStateException

class Tactics(private val stat: Stat) : MapSkillMethod<Unit> {

    override fun apply(battleState: BattleState, self: HeroUnit) {
        val pos = battleState.reverseMap[self]?: throw IllegalStateException()
        val team = battleState.unitsAndPosSeq(self.team).toList()
        val counts = team.asSequence().map { it.key }.groupingBy { it.moveType }.eachCount()
        team.filterNot { it.key == self }.filter { counts[it.key.moveType] ?: 0 <= 2 }.filter {
            it.value.distanceTo(pos) <= 2
        }.forEach {
            it.key.applyBuff(stat)
        }
    }
}