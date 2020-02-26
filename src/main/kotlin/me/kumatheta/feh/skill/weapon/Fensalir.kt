package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.BasicWeapon
import me.kumatheta.feh.Lance
import me.kumatheta.feh.MapSkillMethod
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.threaten

object Fensalir : BasicWeapon(Lance, 16) {
    override val startOfTurn: MapSkillMethod<Unit>?
        get() = threaten(Stat(atk = -4))
}