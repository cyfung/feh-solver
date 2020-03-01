package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.Passive

object LiveToServe3 : BasicSkill() {
    override val onHealOthers: ((battleState: BattleState, self: HeroUnit, target: HeroUnit, healAmount: Int) -> Unit) =
        { _, self, _, healAmount ->
            self.heal(healAmount)
        }
}