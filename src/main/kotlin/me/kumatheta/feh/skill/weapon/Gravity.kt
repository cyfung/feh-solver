package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.BasicWeapon
import me.kumatheta.feh.CombatEndSkill
import me.kumatheta.feh.NegativeStatus
import me.kumatheta.feh.Staff
import me.kumatheta.feh.skill.negativeStatus

object GravityPlus : BasicWeapon(Staff, 10) {
    override val combatEnd: CombatEndSkill? = negativeStatus(NegativeStatus.GRAVITY, 1, true)
}