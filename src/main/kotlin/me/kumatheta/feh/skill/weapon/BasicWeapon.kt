package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.Axe
import me.kumatheta.feh.BasicWeapon
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Lance
import me.kumatheta.feh.MoveType
import me.kumatheta.feh.Sword

object IronLance: BasicWeapon(Lance, 6)
object SilverLance : BasicWeapon(Lance, 11)

object IronSword: BasicWeapon(Sword, 6)
object SilverSword : BasicWeapon(Sword, 11)

object IronAxe: BasicWeapon(Axe, 6)
object SilverAxe : BasicWeapon(Axe, 11)
object SlayingHammerPlus : BasicWeapon(Axe, 14) {
    override fun isEffective(foe: HeroUnit): Boolean {
        return foe.moveType == MoveType.ARMORED
    }
}