package me.kumatheta.feh.skill.effect.startofturn

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.adjacentAllies
import me.kumatheta.feh.skill.effect.StartOfTurnEffect

class Wave(private val stat: Stat, private val oddTurn: Boolean): StartOfTurnEffect {
    override fun onStartOfTurn(battleState: BattleState, self: HeroUnit) {
        val currentTurnOdd = battleState.turn % 2 == 1
        if (oddTurn == currentTurnOdd) {
            (self.adjacentAllies(battleState) + self).forEach {
                it.applyBuff(stat)
            }
        }
    }
}

fun wave(
    atk: Int = 0,
    spd: Int = 0,
    def: Int = 0,
    res: Int = 0,
    oddTurn: Boolean
) = Wave(Stat(atk = atk, spd = spd, def = def, res = res), oddTurn)