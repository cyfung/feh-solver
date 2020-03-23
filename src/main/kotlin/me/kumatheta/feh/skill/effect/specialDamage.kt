package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.InCombatStat

object DealSpecialDamage: DamageIncrease{
    override fun getDamageIncrease(combatStatus: CombatStatus<InCombatStat>, specialTriggered: Boolean): Int {
        return if (specialTriggered) {
            10
        } else {
            0
        }
    }
}