package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.Axe
import me.kumatheta.feh.BasicWeapon
import me.kumatheta.feh.BowB
import me.kumatheta.feh.BowC
import me.kumatheta.feh.Lance
import me.kumatheta.feh.MagicB
import me.kumatheta.feh.MoveType
import me.kumatheta.feh.Staff
import me.kumatheta.feh.Sword
import me.kumatheta.feh.WeaponType

object IronLance : BasicWeapon(Lance, 6)
object SteelLance : BasicWeapon(Lance, 8)
object SilverLance : BasicWeapon(Lance, 11)

object IronSword : BasicWeapon(Sword, 6)
object SilverSword : BasicWeapon(Sword, 11)
object ArmorsmasherPlus : BasicWeapon(Sword, 14) {
    override val effectiveAgainstMoveType: Set<MoveType>? = setOf(MoveType.ARMORED)
}

object IronAxe : BasicWeapon(Axe, 6)
object SilverAxe : BasicWeapon(Axe, 11)
object SlayingHammerPlus : BasicWeapon(Axe, 14) {
    override val effectiveAgainstMoveType: Set<MoveType>? = setOf(MoveType.ARMORED)
}

object SteelBow : BasicWeapon(BowC, 6)

object Assault : BasicWeapon(Staff, 10)

object SlayingBowB : SlayingWeapon(BowB, 14)

abstract class SlayingWeapon(weaponType: WeaponType, might: Int) : BasicWeapon(weaponType, might) {
    override val coolDownCountAdj: Int = -1
}

object ThoronPlus : BasicWeapon(MagicB, 13)