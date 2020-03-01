package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.WeaponType
import me.kumatheta.feh.skill.*

fun WeaponType.withSkill(
    might: Int,
    basicSkill: BasicSkill,
    hp: Int = 0,
    spd: Int = 0,
    def: Int = 0,
    res: Int = 0
): BasicWeapon {
    if (basicSkill.extraStat != null) {
        throw IllegalArgumentException("skill has stat as well")
    }
    val weaponStat = weaponStat(might = might, hp = hp, spd = spd, def = def, res = res)
    return BasicWeapon(
        this, basicSkill.copy(
            extraStat = weaponStat
        )
    )
}