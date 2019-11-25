package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.*

object MysticBoost3 : Passive {
    override val denyAdaptiveDamage: CombatStartSkill<Boolean>?
        get() = combatStartSkillTrue
    override val denyStaffAsNormal: CombatStartSkill<Boolean>?
        get() = combatStartSkillTrue
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