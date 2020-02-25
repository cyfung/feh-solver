package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.BasicWeapon
import me.kumatheta.feh.InCombatSkill
import me.kumatheta.feh.Stat
import me.kumatheta.feh.WeaponType

class BladeTome(weaponType: WeaponType, might: Int) : BasicWeapon(weaponType, might) {
    override val coolDownCountAdj: Int
        get() = 1

    override val additionalInCombatStat: InCombatSkill<Stat>? = {
        Stat(atk = it.self.bonus.totalExceptHp)
    }
}


fun WeaponType.bladeTome(might: Int): BladeTome {
    return BladeTome(this, might)
}