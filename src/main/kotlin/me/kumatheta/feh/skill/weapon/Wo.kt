package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.BasicWeapon
import me.kumatheta.feh.PerAttackSkill
import me.kumatheta.feh.Stat
import me.kumatheta.feh.WeaponType
import me.kumatheta.feh.skill.effect.specialDamage

class SpecialDamageWeapon(weaponType: WeaponType, might: Int, extraStat: Stat = Stat.ZERO) :
    BasicWeapon(weaponType, might, extraStat) {
    override val damageIncrease: PerAttackSkill<Int>?
        get() = specialDamage
}


fun WeaponType.specialDamage(might: Int, extraStat: Stat = Stat.ZERO): SpecialDamageWeapon {
    return SpecialDamageWeapon(this, might, extraStat)
}