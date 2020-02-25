package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.BasicWeapon
import me.kumatheta.feh.InCombatSkill
import me.kumatheta.feh.WeaponType
import me.kumatheta.feh.combatSkill

class Raven(weaponType: WeaponType, might: Int) : BasicWeapon(weaponType, might) {
    override val triangleAdept: InCombatSkill<Int>? = combatSkill(20)
}

fun WeaponType.raven(might: Int): Raven {
    return Raven(this, might)
}
