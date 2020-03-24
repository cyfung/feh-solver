package me.kumatheta.feh.skill.effect.startofturn

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.MoveType
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.allies
import me.kumatheta.feh.skill.effect.StartOfTurnEffect

class Opening<R : Comparable<R>>(
    private val stat: Stat,
    private val selector: (HeroUnit) -> R
) : StartOfTurnEffect {
    override fun onStartOfTurn(battleState: BattleState, self: HeroUnit) {
        self.allies(battleState).maxBy(selector)?.cachedEffect?.applyBuff(stat)
    }
}

inline fun <reified R : Comparable<R>> opening(
    atk: Int = 0,
    spd: Int = 0,
    def: Int = 0,
    res: Int = 0,
    noinline selector: (HeroUnit) -> R
) = Opening(Stat(atk = atk, spd = spd, def = def, res = res), selector)