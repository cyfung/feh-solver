package me.kumatheta.feh.skill

import me.kumatheta.feh.*
import me.kumatheta.feh.skill.effect.stance
import me.kumatheta.feh.skill.weapon.*

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
    "Slaying Edge+ Def" to Sword.slaying(14, Stat(hp = 5, def = 4)),
    "Thoron+" to MagicB.basic(13),
    "Rexcailbur+ Atk" to MagicG.basic(15,extraStat = Stat(hp=2)),
    "Silver Axe+ Def" to Axe.basic(16,extraStat = Stat(hp=5, def=4)),
    "Raudrblade+" to MagicR.bladeTome(13),
    "Gronnowl+" to MagicG.owl(10),
    "Blarowl+" to MagicB.owl(10),
    "Blarowl+ Spd" to MagicB.owl(10, Stat(hp = 2, spd = 2)),
    "Keen Gronnwolf+ G Eff" to MagicG.effectiveAndNeutralize(12, MoveType.CAVALRY),
    "Armorsmasher+ Eff" to Sword.effectiveAndNeutralize(14, MoveType.ARMORED, extraStat = Stat(hp = 3)),
    "Zanbato+ Eff" to Sword.effectiveAndNeutralize(14, MoveType.CAVALRY, extraStat = Stat(hp = 3)),
    "Wo Gun+ Def" to Axe.specialDamage(14, extraStat = Stat(hp = 5, def = 4)),
    "Shining Bow+C Atk" to BowC.shining(13, Stat(hp=2)),
    "Barrier Lance+ Res" to Lance.withInCombatStat(14, stance(Stat(res=7)), Stat(hp=5,def=4)),

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
    "Sublime Surge" to SublimeSurge,
    "Ragnell" to Ragnell,
    "Trilemma+ Dazzling" to TrilemmaPlus(disableCounter = true, staffAsNormal = false),

    "Brave Bow+C" to BowC.brave(7),
    "Brave Bow+R" to BowR.brave(7),
    "Brave Sword+" to Sword.brave(8),
    "Brave Lance+" to Lance.brave(8),
    "Brave Axe+" to Axe.brave(8),
    "Blarserpent+ atk" to MagicB.serpent(12, Stat(hp = 2, atk = 1)),
    "Guard Bow+C" to BowC.serpent(12),
    "Guard Bow+G" to BowG.serpent(12),
    "Gronnraven+" to MagicG.raven(11),
    "Emerald Axe+" to Axe.triangleAdept(12)
).toSkillMap()

private fun WeaponType.basic(might: Int, extraStat: Stat = Stat.ZERO): BasicWeapon {
    return BasicWeapon(this, might, extraStat)
}

private fun WeaponType.effective(might: Int, vararg moveTypes: MoveType, extraStat: Stat = Stat.ZERO): BasicWeapon {
    return if (moveTypes.isEmpty()) {
        BasicWeapon(this, might)
    } else {
        object : BasicWeapon(this, might, extraStat) {
            override val effectiveAgainstMoveType: Set<MoveType>? = moveTypes.toSet()
        }
    }
}

