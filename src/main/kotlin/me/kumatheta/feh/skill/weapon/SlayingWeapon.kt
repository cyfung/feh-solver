package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.BasicWeapon
import me.kumatheta.feh.WeaponType

class SlayingWeapon(weaponType: WeaponType, might: Int) : BasicWeapon(weaponType, might) {
    override val coolDownCountAdj: Int = -1
}

fun WeaponType.slaying(might: Int): SlayingWeapon {
    return SlayingWeapon(this, might)
}