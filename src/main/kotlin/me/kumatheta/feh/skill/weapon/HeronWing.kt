package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.BeastC
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.basic
import me.kumatheta.feh.skill.effect.StartOfTurnEffect
import me.kumatheta.feh.skill.effect.others.FlyingBeast
import me.kumatheta.feh.skill.effect.skillEffects
import me.kumatheta.feh.skill.nearbyAllies
import me.kumatheta.feh.skill.plus

val HeronWing = BeastC.basic(14) + skillEffects(
    object : StartOfTurnEffect {
        override fun onStartOfTurn(battleState: BattleState, self: HeroUnit) {
            self.nearbyAllies(battleState, 2).forEach {
                it.cachedEffect.heal(7)
            }
        }
    },
    FlyingBeast
)