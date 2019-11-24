package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.InCombatSkill
import me.kumatheta.feh.InCombatStatus
import me.kumatheta.feh.Passive
import me.kumatheta.feh.CombatStartSkill
import me.kumatheta.feh.Stat

object WardingBreath : Passive {
    override val inCombatStat: CombatStartSkill<Stat>?
        get() = object : CombatStartSkill<Stat> {
            override fun apply(
                battleState: BattleState,
                self: HeroUnit,
                foe: HeroUnit,
                attack: Boolean
            ): Stat {
                return if (attack) {
                    Stat.ZERO
                } else {
                    Stat(res = 4)
                }
            }
        }
    override val cooldownBuff: InCombatSkill<Int>?
        get() = object : InCombatSkill<Int> {
            override fun apply(
                battleState: BattleState,
                self: InCombatStatus,
                foe: InCombatStatus,
                attack: Boolean
            ): Int {
                return if (attack) {
                    0
                } else {
                    1
                }
            }
        }
}