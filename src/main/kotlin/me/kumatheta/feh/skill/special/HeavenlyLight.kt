package me.kumatheta.feh.skill.special

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.skill.HealingSpecial
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.allies

object HeavenlyLight : HealingSpecial(2) {
    override fun trigger(self: HeroUnit, target: HeroUnit, battleState: BattleState) {
        self.allies(battleState).filterNot { it==target }.forEach {
            it.heal(10)
        }
    }
}