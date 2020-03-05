package me.kumatheta.feh.skill

import me.kumatheta.feh.*
import me.kumatheta.feh.skill.effect.dealSpecialDamage
import me.kumatheta.feh.skill.effect.incombatstat.rangeDefStat
import me.kumatheta.feh.skill.effect.incombatstat.stance
import me.kumatheta.feh.skill.weapon.*

val ALL_WEAPONS: SkillMap<BasicWeapon> = sequenceOf(
    "Flametongue+" to DragonC.basic(15),

    "Flametongue+*S" to DragonC.withSkill(16, DragonAdaptive, hp = 5, spd = 3),
    "Slaying Edge+*S" to Sword.slaying(14, hp = 5, spd = 3),
    "Thoron+*S" to MagicB.basic(14, hp = 2, spd = 2),
    "Rexcailbur+*S" to MagicG.basic(14, hp = 2, spd = 2),
    "Silver Axe+*S" to Axe.basic(16, hp = 5, spd =3),
    "Wo Gun+*S" to Axe.withSkill(14, dealSpecialDamage, hp = 5, spd = 3),
    "Blarowl+*S" to MagicB.owl(10, hp = 2, spd = 2),
    "Shining Bow+*S" to BowC.shining(12, hp = 2, spd=2),
    "Barrier Lance+*S" to Lance.withSkill(14, stance(Stat(res = 7)), hp = 5, spd = 3),
    "Dark Breath+*S" to DragonC.darkBreathPlusRefine(14, hp = 5, spd = 3),
    "Water Breath+*S" to waterBreathPlusS,
    "Slaying Bow+*S" to BowC.slaying(12, hp = 2, spd = 2),
    "Slaying Lance+*S" to Lance.slaying(14, hp = 5, spd = 3),
    "Slaying Axe+*S" to Axe.slaying(14, hp = 5, spd = 3),

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
    "Silver Axe+" to Axe.basic(15),
    "Slaying Hammer" to Axe.effective(10, MoveType.ARMORED),
    "Slaying Hammer+" to Axe.effective(14, MoveType.ARMORED),
    "Steel Bow" to BowC.basic(6),
    "Slaying Bow+" to BowC.slaying(12),
    "Assault" to Staff.basic(10),
    "Slaying Axe+" to Axe.slaying(14),
    "Thoron+" to MagicB.basic(13),
    "Raudrblade+" to MagicR.bladeTome(13),
    "Gronnowl+" to MagicG.owl(10),
    "Blarowl+" to MagicB.owl(10),
    "Ridersbane+ Eff" to Lance.effective(14, MoveType.CAVALRY, neutralizeBonus = true, hp = 3),
    "Keen Gronnwolf+ Eff" to MagicG.effective(12, MoveType.CAVALRY, neutralizeBonus = true),
    "Armorsmasher+ Eff" to Sword.effective(14, MoveType.ARMORED, neutralizeBonus = true, hp = 3),
    "Zanbato+ Eff" to Sword.effective(14, MoveType.CAVALRY, neutralizeBonus = true, hp = 3),

    "Slow+" to slowPlus,
    "Gravity+" to gravityPlus,

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
    "Hidskjalf" to Hidskjalf,
    "Fell Breath" to FellBreath,

    "Trilemma+ Dazzling" to trilemmaPlus(disableCounter = true, staffAsNormal = false),

    "Brave Bow+" to BowC.brave(7),
    "Brave Sword+" to Sword.brave(8),
    "Brave Lance+" to Lance.brave(8),
    "Brave Axe+" to Axe.brave(8),
    "Blarserpent+*S" to MagicB.withSkill(
        12,
        rangeDefStat(Stat(def = 6, res = 6)),
        hp = 2, spd = 2
    ),
    "Guard Bow+" to BowC.withSkill(
        12,
        rangeDefStat(Stat(def = 6, res = 6))
    ),
    "Gronnraven+" to MagicG.withSkill(11, BasicSkill(raven = inCombatSkillTrue)),
    "Emerald Axe+" to Axe.withSkill(12, BasicSkill(triangleAdept = combatSkill(20)))
).toSkillMap()

fun weaponStat(might: Int, hp: Int = 0, spd: Int = 0, def: Int = 0, res: Int = 0): Stat {
    return Stat(hp = hp, atk = might, spd = spd, def = def, res = res)
}

private fun WeaponType.basic(might: Int, hp: Int = 0, spd: Int = 0, def: Int = 0, res: Int = 0): BasicWeapon =
    BasicWeapon(
        this,
        BasicSkill(extraStat = weaponStat(might = might, hp = hp, spd = spd, def = def, res = res))
    )

private fun WeaponType.effective(
    might: Int,
    moveType: MoveType,
    neutralizeBonus: Boolean = false,
    hp: Int = 0,
    spd: Int = 0,
    def: Int = 0,
    res: Int = 0
): BasicWeapon {
    val foeEffect = if (neutralizeBonus) {
        val neutralizeEffect = BasicSkill(neutralizeBonus = combatStartSkill(Stat.ZERO))
        val effect: CombatStartSkill<Skill?>? = {
            if (it.foe.moveType == moveType) {
                neutralizeEffect
            } else {
                null
            }
        }
        effect
    } else {
        null
    }
    return BasicWeapon(
        this, BasicSkill(
            extraStat = weaponStat(might = might, hp = hp, spd = spd, def = def, res = res),
            effectiveAgainstMoveType = setOf(moveType),
            foeEffect = foeEffect
        )
    )
}

private fun WeaponType.slaying(might: Int, hp: Int = 0, spd: Int = 0, def: Int = 0, res: Int = 0): BasicWeapon =
    BasicWeapon(
        this, BasicSkill(
            extraStat = weaponStat(might = might, hp = hp, spd = spd, def = def, res = res),
            coolDownCountAdj = -1
        )
    )

private fun WeaponType.bladeTome(might: Int, hp: Int = 0, spd: Int = 0, def: Int = 0, res: Int = 0): BasicWeapon =
    BasicWeapon(
        this, BasicSkill(
            extraStat = weaponStat(might = might, hp = hp, spd = spd, def = def, res = res),
            coolDownCountAdj = 1,
            additionalInCombatStat = {
                Stat(atk = it.self.bonus.totalExceptHp)
            }
        )
    )

private fun WeaponType.owl(might: Int, hp: Int = 0, spd: Int = 0, def: Int = 0, res: Int = 0): BasicWeapon =
    BasicWeapon(
        this, BasicSkill(
            extraStat = weaponStat(might = might, hp = hp, spd = spd, def = def, res = res),
            inCombatStat = { combatStatus ->
                val (battleState, self) = combatStatus
                val buff = self.adjacentAllies(battleState).count() * 2
                Stat(atk = buff, spd = buff, def = buff, res = buff)
            }
        )
    )
