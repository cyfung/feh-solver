package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.MagicR
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.*

val GrimaTruth = BasicWeapon(
    MagicR, BasicSkill(
        extraStat = weaponStat(14, def = 3),
        combatEnd = { combatStatus, attacked ->
            if (attacked) {
                aoeDebuffFoe(combatStatus, Stat(atk = -5, spd = -5))
                aoeBuffAlly(combatStatus, Stat(atk = 5, spd = 5))
            }
        }
    )
)