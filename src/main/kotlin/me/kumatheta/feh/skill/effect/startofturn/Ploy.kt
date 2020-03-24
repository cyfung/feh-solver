package me.kumatheta.feh.skill.effect.startofturn

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat
import me.kumatheta.feh.foe
import me.kumatheta.feh.skill.effect.StartOfTurnEffect
import me.kumatheta.feh.skill.inCardinalDirection

class Ploy(
    private val f: (self: HeroUnit, foe: HeroUnit) -> Unit
) : StartOfTurnEffect {
    override fun onStartOfTurn(battleState: BattleState, self: HeroUnit) {
        battleState.unitsSeq(self.team.foe).filter {
            self.inCardinalDirection(it)
        }.forEach {
            f(self, it)
        }
    }
}

fun resBasedPloy3(stat: Stat) = Ploy { self, foe ->
    if (foe.visibleStat.res < self.startOfTurnStat.res) {
        foe.cachedEffect.applyDebuff(stat)
    }
}
fun resBasedPloy3(atk: Int = 0,
            spd: Int = 0,
            def: Int = 0,
            res: Int = 0
) = resBasedPloy3(Stat(atk = atk, spd = spd, def = def, res = res))