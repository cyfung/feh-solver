package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.BasicWeapon
import me.kumatheta.feh.InCombatSkill
import me.kumatheta.feh.MagicB
import me.kumatheta.feh.Stat
import me.kumatheta.feh.WeaponType
import me.kumatheta.feh.inCombatSkillTrue

class BraveWeapon(weaponType: WeaponType, might: Int, override val extraStat: Stat = Stat(spd = -5)): BasicWeapon(weaponType, might) {
    override val brave: InCombatSkill<Boolean>?
        get() = inCombatSkillTrue
}

fun WeaponType.brave(might: Int): BraveWeapon {
    return BraveWeapon(this, might)
}