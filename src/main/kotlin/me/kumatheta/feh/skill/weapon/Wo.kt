package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.*
import me.kumatheta.feh.skill.effect.specialDamage

class SpecialDamageWeapon(weaponType: WeaponType, might: Int, extraStat: Stat = Stat.ZERO) :
    BasicWeapon(weaponType, might, extraStat) {
    override val damageIncrease: ((CombatStatus<InCombatStat>, specialTriggered: Boolean) -> Int)?
        get() = specialDamage
}


fun WeaponType.specialDamage(might: Int, extraStat: Stat = Stat.ZERO): SpecialDamageWeapon {
    return SpecialDamageWeapon(this, might, extraStat)
}