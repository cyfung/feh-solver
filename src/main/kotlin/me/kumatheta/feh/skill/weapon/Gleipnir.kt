package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.MagicR
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.BasicWeapon
import me.kumatheta.feh.skill.weaponStat

private val IN_COMBAT_BUF = Stat(atk = 3, spd = 3)

val Gleipnir = BasicWeapon(
    MagicR, BasicSkill(
        extraStat = weaponStat(14, res = 3),
        inCombatStat = { combatStatus ->
            if (combatStatus.foe.currentHp == combatStatus.foe.maxHp) {
                IN_COMBAT_BUF
            } else {
                Stat.ZERO
            }
        }
    )
)