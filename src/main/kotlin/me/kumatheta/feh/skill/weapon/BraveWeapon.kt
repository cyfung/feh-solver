package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.WeaponType
import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.BasicWeapon
import me.kumatheta.feh.skill.weaponStat

fun WeaponType.brave(might: Int, hp: Int = 0, spd: Int = -5, def: Int = 0, res: Int = 0): BasicWeapon =
    BasicWeapon(
        this, BasicSkill(
            extraStat = weaponStat(might = might, hp = hp, spd = spd, def = def, res = res),
            brave = {
                it.initAttack
            }
        )
    )