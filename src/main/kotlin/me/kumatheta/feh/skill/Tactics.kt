package me.kumatheta.feh.skill

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.MapSkillMethod
import me.kumatheta.feh.Stat
import java.lang.IllegalStateException

class Tactics(private val stat: Stat) : MapSkillMethod<Unit> {

    override fun apply(battleState: BattleState, self: HeroUnit) {
        val pos = self.position
        val team = battleState.unitsSeq(self.team).toList()
        val counts = team.asSequence().groupingBy { it.moveType }.eachCount()
        team.filterNot { it == self }.filter { counts[it.moveType] ?: 0 <= 2 }.filter {
            it.position.distanceTo(pos) <= 2
        }.forEach {
            it.applyBuff(stat)
        }
    }
}