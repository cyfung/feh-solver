package me.kumatheta.feh.skill.effect.startofturn

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat
import me.kumatheta.feh.foe
import me.kumatheta.feh.skill.effect.StartOfTurnEffect

class Threaten(private val stat: Stat): StartOfTurnEffect {
    override fun onStartOfTurn(battleState: BattleState, self: HeroUnit) {
        val position = self.position
        battleState.unitsSeq(self.team.foe).filter { it.position.distanceTo(position) <= 2 }.forEach {
            it.applyDebuff(stat)
        }
    }
}

fun threaten(atk: Int = 0,
             spd: Int = 0,
             def: Int = 0,
             res: Int = 0
) = Threaten(Stat(atk = atk, spd = spd, def = def, res = res))