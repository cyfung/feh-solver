package me.kumatheta.feh.skill.effect.startofturn

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.effect.StartOfTurnEffect

class Tactics(private val stat: Stat) : StartOfTurnEffect {
    override fun onStartOfTurn(battleState: BattleState, self: HeroUnit) {
        val pos = self.position
        val team = battleState.unitsSeq(self.team).toList()
        val counts = team.asSequence().groupingBy { it.moveType }.eachCount()
        team.filterNot { it == self }.filter { counts[it.moveType] ?: 0 <= 2 }.filter {
            it.position.distanceTo(pos) <= 2
        }.forEach {
            it.cachedEffect.applyBuff(stat)
        }
    }
}

fun tactics(atk: Int = 0,
          spd: Int = 0,
          def: Int = 0,
          res: Int = 0
) = Tactics(Stat(atk = atk, spd = spd, def = def, res = res))