package me.kumatheta.feh.skill.effect.startofturn

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat
import me.kumatheta.feh.foe
import me.kumatheta.feh.skill.effect.StartOfTurnEffect

class Chill<R : Comparable<R>>(
    private val debuff: Stat,
    private val criteria: (HeroUnit) -> R
) : StartOfTurnEffect {
    override fun onStartOfTurn(battleState: BattleState, self: HeroUnit) {
        battleState.unitsSeq(self.team.foe).maxBy {
            criteria(it)
        }?.applyDebuff(debuff)
    }
}

inline fun <reified R : Comparable<R>> chill(
    atk: Int = 0,
    spd: Int = 0,
    def: Int = 0,
    res: Int = 0,
    noinline criteria: (HeroUnit) -> R
) = Chill(Stat(atk = atk, spd = spd, def = def, res = res), criteria)