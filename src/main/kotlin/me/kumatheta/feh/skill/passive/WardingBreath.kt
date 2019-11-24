package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.CombatStartSkill
import me.kumatheta.feh.CooldownChange
import me.kumatheta.feh.InCombatSkill
import me.kumatheta.feh.InCombatStatus
import me.kumatheta.feh.Passive
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.BlowOrStance

object WardingBreath : Passive {
    override val inCombatStat: CombatStartSkill<Stat>?
        get() = BlowOrStance(Stat.ZERO, Stat(res = 4))

    override val cooldownBuff: InCombatSkill<CooldownChange<Int>>?
        get() = object : InCombatSkill<CooldownChange<Int>> {
            override fun apply(
                battleState: BattleState,
                self: InCombatStatus,
                foe: InCombatStatus,
                initAttack: Boolean
            ): CooldownChange<Int> {
                return if (initAttack) {
                    CooldownChange(0, 0)
                } else {
                    CooldownChange(1, 1)
                }
            }
        }
}