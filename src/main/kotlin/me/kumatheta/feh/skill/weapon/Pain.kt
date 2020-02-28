package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.BasicWeapon
import me.kumatheta.feh.CombatEndSkill
import me.kumatheta.feh.Staff

object Pain : BasicWeapon(Staff, 3) {
    override val combatEnd: CombatEndSkill? = { combatStatus, attacked ->
        if (attacked && !combatStatus.foe.heroUnit.isDead) {
            combatStatus.foe.heroUnit.cachedEffect.takeNonLethalDamage(10)
        }
    }
}
