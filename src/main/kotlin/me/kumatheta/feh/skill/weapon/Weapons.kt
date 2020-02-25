package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.Axe
import me.kumatheta.feh.BasicWeapon
import me.kumatheta.feh.BowB
import me.kumatheta.feh.BowC
import me.kumatheta.feh.Lance
import me.kumatheta.feh.MagicB
import me.kumatheta.feh.MagicR
import me.kumatheta.feh.MoveType
import me.kumatheta.feh.Staff
import me.kumatheta.feh.Stat
import me.kumatheta.feh.Sword
import me.kumatheta.feh.WeaponType
import me.kumatheta.feh.skill.SkillMap
import me.kumatheta.feh.skill.toSkillMap

val ALL_WEAPONS: SkillMap<BasicWeapon> = sequenceOf(
    "Iron Lance" to Lance.basic(6),
    "Steel Lance" to Lance.basic(8),
    "Silver Lance" to Lance.basic(11),
    "Iron Sword" to Sword.basic(6),
    "Steel Sword" to Sword.basic(8),
    "Silver Sword" to Sword.basic(11),
    "Armorsmasher+" to Sword.effective(14, MoveType.ARMORED),
    "Iron Axe" to Axe.basic(6),
    "Steel Axe" to Axe.basic(8),
    "Silver Axe" to Axe.basic(11),
    "Slaying Hammer" to Axe.effective(10, MoveType.ARMORED),
    "Slaying Hammer+" to Axe.effective(14, MoveType.ARMORED),
    "Steel Bow" to BowC.basic(6),
    "Assault" to Staff.basic(10),
    "Slaying Bow+ B" to BowB.slaying(12),
    "Thoron+" to MagicB.basic(13),
    "Raudrblade+" to MagicR.bladeTome(13),

    "Slow+" to SlowPlus,

    "Draconic Poleax Eff" to DraconicPoleaxEff,
    "Lyfjaberg" to Lyfjaberg,
    "Leiptr" to Leiptr,
    "Dire Thunder" to DireThunder,
    "Grima's Truth" to GrimaTruth,

    "Blarserpent+ atk" to MagicB.serpent(12, Stat(hp=2, atk=1)),
    "Guard Bow+" to BowC.serpent(12)
).toSkillMap()

private fun WeaponType.basic(might: Int): BasicWeapon {
    return BasicWeapon(this, might)
}

private fun WeaponType.effective(might: Int, vararg moveTypes: MoveType): BasicWeapon {
    return if (moveTypes.isEmpty()) {
        BasicWeapon(this, might)
    } else {
        object : BasicWeapon(this, might) {
            override val effectiveAgainstMoveType: Set<MoveType>? = moveTypes.toSet()
        }
    }
}

