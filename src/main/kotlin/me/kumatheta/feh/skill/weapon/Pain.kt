package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.*

object Pain : BasicWeapon(Staff, 3) {
    override val endOfCombat: CombatEndSkillMethod? = object : CombatEndSkillMethod {
        override fun apply(
            battleState: BattleState,
            self: HeroUnit,
            foe: HeroUnit,
            attack: Boolean,
            attacked: Boolean
        ) {
            if (attacked) {
                foe.endOfCombatEffects.takeNonLethalDamage(10)
            }
        }
    }
}