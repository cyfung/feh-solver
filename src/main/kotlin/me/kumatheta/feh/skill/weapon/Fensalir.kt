package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.Lance
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.BasicWeapon
import me.kumatheta.feh.skill.effect.threaten
import me.kumatheta.feh.skill.weaponStat

object Fensalir : BasicWeapon(
    Lance, BasicSkill(
        extraStat = weaponStat(16),
        startOfTurn = threaten(Stat(atk = -4))
    )
)