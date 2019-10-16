package com.bloombase.feh.skill.passive

import com.bloombase.feh.*

object WardingBreath : Passive {
    override val buff: CombatSkillMethod<Stat>?
        get() = object : CombatSkillMethod<Stat> {
            override fun apply(battleState: BattleState, self: HeroUnit, opponent: HeroUnit, attack: Boolean): Stat {
                return if (attack) {
                    Stat.ZERO
                } else {
                    Stat(res = 4)
                }
            }
        }
    override val cooldownBuff: CombatSkillMethod<Int>?
        get() = object : CombatSkillMethod<Int> {
            override fun apply(battleState: BattleState, self: HeroUnit, opponent: HeroUnit, attack: Boolean): Int {
                return if (attack) {
                    0
                } else {
                    1
                }
            }
        }
}