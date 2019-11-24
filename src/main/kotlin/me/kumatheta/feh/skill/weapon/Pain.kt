package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.*

object Pain : BasicWeapon(Staff, 3) {
    override val combatEnd: CombatEndSkill? = object : CombatEndSkill {
        override fun apply(
            battleState: BattleState,
            self: HeroUnit,
            foe: HeroUnit,
            attack: Boolean,
            attacked: Boolean
        ) {
            if (attacked && !foe.isDead) {
                foe.endOfCombatEffects.takeNonLethalDamage(10)
            }
        }
    }
}