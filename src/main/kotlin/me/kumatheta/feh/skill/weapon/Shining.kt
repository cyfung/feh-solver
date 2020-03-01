package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.Stat
import me.kumatheta.feh.WeaponType
import me.kumatheta.feh.skill.*

fun WeaponType.shining(might: Int, hp: Int = 0, spd: Int = 0, def: Int = 0, res: Int = 0): BasicWeapon =
    BasicWeapon(
        this, BasicSkill(
            extraStat = weaponStat(might = might, hp = hp, spd = spd, def = def, res = res),
            damageIncrease = { combatStatus, _ ->
                if (combatStatus.foe.inCombatStat.def >= combatStatus.foe.inCombatStat.res + 5) {
                    7
                } else {
                    0
                }
            }
        )
    )