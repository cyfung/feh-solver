package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.CooldownChange
import me.kumatheta.feh.InCombatSkill
import me.kumatheta.feh.InCombatStat
import me.kumatheta.feh.Passive

object Guard3 : Passive {
    override val cooldownDebuff: InCombatSkill<CooldownChange<Int>>? = object : InCombatSkill<CooldownChange<Int>> {
        override fun apply(
            battleState: BattleState,
            self: InCombatStat,
            foe: InCombatStat,
            initAttack: Boolean
        ): CooldownChange<Int> {
            return if (self.heroUnit.hpThreshold(80) >= 0) {
                CooldownChange(1, 1)
            } else {
                CooldownChange(0, 0)
            }
        }

    }
}