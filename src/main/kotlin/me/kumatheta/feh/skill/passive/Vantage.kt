package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.InCombatSkill
import me.kumatheta.feh.InCombatStat
import me.kumatheta.feh.Passive

object Vantage3 : Passive {
    override val vantage: InCombatSkill<Boolean>? = object : InCombatSkill<Boolean> {
        override fun apply(
            battleState: BattleState,
            self: InCombatStat,
            foe: InCombatStat,
            initAttack: Boolean
        ): Boolean {
            return self.heroUnit.hpThreshold(75) <= 0
        }
    }
}