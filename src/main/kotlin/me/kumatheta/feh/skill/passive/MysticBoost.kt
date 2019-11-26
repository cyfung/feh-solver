package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.CombatEndSkill
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.InCombatSkill
import me.kumatheta.feh.Passive
import me.kumatheta.feh.inCombatSkillTrue

object MysticBoost3 : Passive {
    override val denyAdaptiveDamage: InCombatSkill<Boolean>?
        get() = inCombatSkillTrue
    override val denyStaffAsNormal: InCombatSkill<Boolean>?
        get() = inCombatSkillTrue
    override val combatEnd: CombatEndSkill? = object : CombatEndSkill {
        override fun apply(
            battleState: BattleState,
            self: HeroUnit,
            foe: HeroUnit,
            attack: Boolean,
            attacked: Boolean
        ) {
            self.endOfCombatEffects.heal(6)
        }
    }
}