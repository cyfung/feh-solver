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
