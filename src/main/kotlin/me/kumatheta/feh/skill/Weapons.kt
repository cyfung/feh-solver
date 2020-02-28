package me.kumatheta.feh.skill

import me.kumatheta.feh.Axe
import me.kumatheta.feh.BasicWeapon
import me.kumatheta.feh.BowB
import me.kumatheta.feh.BowC
import me.kumatheta.feh.BowG
import me.kumatheta.feh.BowR
import me.kumatheta.feh.DragonG
import me.kumatheta.feh.Lance
import me.kumatheta.feh.MagicB
import me.kumatheta.feh.MagicG
import me.kumatheta.feh.MagicR
import me.kumatheta.feh.MoveType
import me.kumatheta.feh.Staff
import me.kumatheta.feh.Stat
import me.kumatheta.feh.Sword
import me.kumatheta.feh.WeaponType
import me.kumatheta.feh.skill.weapon.DraconicPoleaxEff
import me.kumatheta.feh.skill.weapon.FlowerOfJoy
import me.kumatheta.feh.skill.weapon.Gleipnir
import me.kumatheta.feh.skill.weapon.GravityPlus
import me.kumatheta.feh.skill.weapon.GrimaTruth
import me.kumatheta.feh.skill.weapon.Leiptr
import me.kumatheta.feh.skill.weapon.Lyfjaberg
import me.kumatheta.feh.skill.weapon.Ragnarok
import me.kumatheta.feh.skill.weapon.Randgridr
import me.kumatheta.feh.skill.weapon.SlowPlus
import me.kumatheta.feh.skill.weapon.bladeTome
import me.kumatheta.feh.skill.weapon.brave
import me.kumatheta.feh.skill.weapon.owl
import me.kumatheta.feh.skill.weapon.raven
import me.kumatheta.feh.skill.weapon.serpent
import me.kumatheta.feh.skill.weapon.slaying
import me.kumatheta.feh.skill.weapon.triangleAdept

val ALL_WEAPONS: SkillMap<BasicWeapon> = sequenceOf(
    "Iron Lance" to Lance.basic(6),
    "Steel Lance" to Lance.basic(8),
    "Silver Lance" to Lance.basic(11),
    "Iron Sword" to Sword.basic(6),
    "Steel Sword" to Sword.basic(8),
    "Silver Sword" to Sword.basic(11),
    "Flametongue+G" to DragonG.basic(15),
    "Armorsmasher+" to Sword.effective(14, MoveType.ARMORED),
    "Iron Axe" to Axe.basic(6),
    "Steel Axe" to Axe.basic(8),
    "Silver Axe" to Axe.basic(11),
    "Silver Axe+" to Axe.basic(15),
    "Slaying Hammer" to Axe.effective(10, MoveType.ARMORED),
    "Slaying Hammer+" to Axe.effective(14, MoveType.ARMORED),
    "Steel Bow C" to BowC.basic(6),
    "Assault" to Staff.basic(10),
    "Slaying Bow+ B" to BowB.slaying(12),
    "Slaying Bow+ G" to BowG.slaying(12),
    "Slaying Axe+" to Axe.slaying(14),
    "Thoron+" to MagicB.basic(13),
    "Raudrblade+" to MagicR.bladeTome(13),
    "Gronnowl+" to MagicG.owl(10),
    "Blarowl+" to MagicB.owl(10),

    "Slow+" to SlowPlus,
    "Gravity+" to GravityPlus,

    "Draconic Poleax Eff" to DraconicPoleaxEff,
    "Lyfjaberg" to Lyfjaberg,
    "Leiptr" to Leiptr,
    "Dire Thunder" to MagicB.brave(9),
    "Grima's Truth" to GrimaTruth,
    "Flower of Joy" to FlowerOfJoy,
    "Gleipnir" to Gleipnir,
    "Randgridr" to Randgridr,
    "Ragnarok" to Ragnarok,

    "Brave Bow+C" to BowC.brave(7),
    "Brave Bow+R" to BowR.brave(7),
    "Brave Sword+" to Sword.brave(8),
    "Brave Lance+" to Lance.brave(8),
    "Blarserpent+ atk" to MagicB.serpent(12, Stat(hp = 2, atk = 1)),
    "Guard Bow+C" to BowC.serpent(12),
    "Guard Bow+G" to BowG.serpent(12),
    "Gronnraven+" to MagicG.raven(11),
    "Emerald Axe+" to Axe.triangleAdept(12)
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

