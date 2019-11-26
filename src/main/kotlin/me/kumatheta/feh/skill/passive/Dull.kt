package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.CombatStartSkill
import me.kumatheta.feh.ConstantCombatStartSkill
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Passive
import me.kumatheta.feh.Skill
import me.kumatheta.feh.Stat

object DullClose3 : Passive {
    override val foeEffect: CombatStartSkill<Skill?>? = object : CombatStartSkill<Skill?> {
        override fun apply(battleState: BattleState, self: HeroUnit, foe: HeroUnit, initAttack: Boolean): Skill? {
            return if (foe.weaponType.isRanged) {
                null
            } else {
                object : Skill {
                    override val neutralizeBonus: CombatStartSkill<Stat?>? = ConstantCombatStartSkill(Stat.ZERO)
                }
            }
        }
    }
}