package me.kumatheta.feh.skill.effect.startofturn

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat
import me.kumatheta.feh.foe
import me.kumatheta.feh.skill.effect.StartOfTurnEffect
import me.kumatheta.feh.util.surroundings

class Sabotage(private val stat: Stat) : StartOfTurnEffect {
    override fun onStartOfTurn(battleState: BattleState, self: HeroUnit) {
        val threshold = self.visibleStat.res - 3
        battleState.unitsSeq(self.team.foe).filter { it.visibleStat.res <= threshold }.filter { foe ->
            foe.position.surroundings(battleState.maxPosition).any {
                val heroUnit = battleState.getChessPiece(it) as? HeroUnit ?: return@any false
                heroUnit.team == foe.team
            }
        }.forEach {
            it.cachedEffect.applyDebuff(stat)
        }
    }
}

fun sabotage(atk: Int = 0,
                  spd: Int = 0,
                  def: Int = 0,
                  res: Int = 0
) = Sabotage(Stat(atk = atk, spd = spd, def = def, res = res))