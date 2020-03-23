package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.effect.HealEffect

object LiveToServe3 : HealEffect {
    override fun onHeal(
        battleState: BattleState,
        self: HeroUnit,
        ally: HeroUnit,
        healAmount: Int,
        selfUseHeal: Boolean
    ) {
        if (selfUseHeal) {
            self.heal(healAmount)
        }
    }

}