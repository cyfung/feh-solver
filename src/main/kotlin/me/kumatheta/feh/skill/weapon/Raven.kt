package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.BasicWeapon
import me.kumatheta.feh.InCombatSkill
import me.kumatheta.feh.WeaponType
import me.kumatheta.feh.combatSkill
import me.kumatheta.feh.inCombatSkillTrue

class Raven(weaponType: WeaponType, might: Int) : BasicWeapon(weaponType, might) {
    override val raven: InCombatSkill<Boolean>?
        get() = inCombatSkillTrue
}

fun WeaponType.raven(might: Int): Raven {
    return Raven(this, might)
}
