package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Passive

object LiveToServe3 : Passive {
    override val onHealOthers: ((battleState: BattleState, self: HeroUnit, target: HeroUnit, healAmount: Int) -> Unit) =
        { _, self, _, healAmount ->
            self.heal(healAmount)
        }
}