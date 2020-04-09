package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.Axe
import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.adjacentAlliesAndSelf
import me.kumatheta.feh.skill.basic
import me.kumatheta.feh.skill.effect.StartOfTurnEffect
import me.kumatheta.feh.skill.plus

val Byleistr = Axe.basic(16) + object : StartOfTurnEffect {
    override fun onStartOfTurn(battleState: BattleState, self: HeroUnit) {
        if (battleState.turn % 2 == 1) {
            self.adjacentAlliesAndSelf(battleState).forEach {
                it.cachedEffect.applyBuff(Stat(atk = 4, spd = 4, def = 4, res = 4))
            }
        }
    }

}