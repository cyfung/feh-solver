package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.BasicWeapon
import me.kumatheta.feh.Stat
import me.kumatheta.feh.WeaponType

class SlayingWeapon(weaponType: WeaponType, might: Int, extraStat: Stat) : BasicWeapon(weaponType, might, extraStat) {
    override val coolDownCountAdj: Int = -1
}

fun WeaponType.slaying(might: Int, extraStat: Stat = Stat.ZERO): SlayingWeapon {
    return SlayingWeapon(this, might, extraStat)
}