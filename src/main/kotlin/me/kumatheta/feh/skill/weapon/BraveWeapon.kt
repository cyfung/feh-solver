package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.BasicWeapon
import me.kumatheta.feh.InCombatSkill
import me.kumatheta.feh.Stat
import me.kumatheta.feh.WeaponType

class BraveWeapon(weaponType: WeaponType, might: Int, extraStat: Stat = Stat(spd = -5)) : BasicWeapon(weaponType, might, extraStat) {
    override val brave: InCombatSkill<Boolean>? = {
        it.initAttack
    }
}

fun WeaponType.brave(might: Int): BraveWeapon {
    return BraveWeapon(this, might)
}