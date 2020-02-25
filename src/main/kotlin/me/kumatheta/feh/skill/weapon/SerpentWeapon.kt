package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.BasicWeapon
import me.kumatheta.feh.CombatStartSkill
import me.kumatheta.feh.Stat
import me.kumatheta.feh.WeaponType
import me.kumatheta.feh.skill.rangeDefStat

class SerpentWeapon(weaponType: WeaponType, might: Int, override val extraStat: Stat) : BasicWeapon(weaponType, might) {
    override val inCombatStat: CombatStartSkill<Stat>? = rangeDefStat(Stat(def = 6, res = 6))
}

fun WeaponType.serpent(might: Int, stat: Stat = Stat.ZERO): SerpentWeapon {
    return SerpentWeapon(this, might, stat)
}