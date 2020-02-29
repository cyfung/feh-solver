package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.*

class InCombatStatWeapon(weaponType: WeaponType, might: Int, override val inCombatStat: CombatStartSkill<Stat>?, extraStat: Stat = Stat.ZERO) :
    BasicWeapon(weaponType, might, extraStat)

fun WeaponType.withInCombatStat(might: Int, inCombatStat: CombatStartSkill<Stat>?, extraStat: Stat = Stat.ZERO): InCombatStatWeapon {
    return InCombatStatWeapon(this, might, inCombatStat, extraStat)
}