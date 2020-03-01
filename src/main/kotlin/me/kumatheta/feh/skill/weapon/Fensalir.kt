package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.Lance
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.BasicWeapon
import me.kumatheta.feh.skill.effect.startofturn.threaten
import me.kumatheta.feh.skill.weaponStat

object Fensalir : BasicWeapon(
    Lance, threaten(Stat(atk = -4)).copy(
        extraStat = weaponStat(16)
    )
)