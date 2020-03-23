package me.kumatheta.feh.skill

import me.kumatheta.feh.ALL_STAT_TYPES
import me.kumatheta.feh.Axe
import me.kumatheta.feh.BowC
import me.kumatheta.feh.DragonC
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Lance
import me.kumatheta.feh.MagicB
import me.kumatheta.feh.MagicG
import me.kumatheta.feh.MagicR
import me.kumatheta.feh.MoveType
import me.kumatheta.feh.NegativeStatus
import me.kumatheta.feh.Staff
import me.kumatheta.feh.Stat
import me.kumatheta.feh.StatType
import me.kumatheta.feh.Sword
import me.kumatheta.feh.WeaponType
import me.kumatheta.feh.skill.effect.CoolDownCountAdjust
import me.kumatheta.feh.skill.effect.DealSpecialDamage
import me.kumatheta.feh.skill.effect.DisableCounter
import me.kumatheta.feh.skill.effect.EffectOnFoe
import me.kumatheta.feh.skill.effect.EffectiveAgainstMovement
import me.kumatheta.feh.skill.effect.ExtraInCombatStatEffect
import me.kumatheta.feh.skill.effect.ExtraStat
import me.kumatheta.feh.skill.effect.NeutralizeBonusBasic
import me.kumatheta.feh.skill.effect.RavenBasic
import me.kumatheta.feh.skill.effect.SkillEffect
import me.kumatheta.feh.skill.effect.SlayingEffect
import me.kumatheta.feh.skill.effect.TriangleAdept
import me.kumatheta.feh.skill.effect.incombatstat.OwlEffect
import me.kumatheta.feh.skill.effect.incombatstat.rangeDefStat
import me.kumatheta.feh.skill.effect.incombatstat.stance
import me.kumatheta.feh.skill.effect.others.BraveOnInit
import me.kumatheta.feh.skill.effect.others.DragonAdaptive
import me.kumatheta.feh.skill.effect.others.ShiningEffect
import me.kumatheta.feh.skill.effect.postcombat.aoeDebuff
import me.kumatheta.feh.skill.effect.postcombat.aoeNegativeStatus
import me.kumatheta.feh.skill.effect.skillEffects
import me.kumatheta.feh.skill.weapon.DraconicPoleaxEff
import me.kumatheta.feh.skill.weapon.FellBreath
import me.kumatheta.feh.skill.weapon.FlowerOfJoy
import me.kumatheta.feh.skill.weapon.Gleipnir
import me.kumatheta.feh.skill.weapon.GrimaTruth
import me.kumatheta.feh.skill.weapon.Hidskjalf
import me.kumatheta.feh.skill.weapon.Leiptr
import me.kumatheta.feh.skill.weapon.Lyfjaberg
import me.kumatheta.feh.skill.weapon.Ragnarok
import me.kumatheta.feh.skill.weapon.Ragnell
import me.kumatheta.feh.skill.weapon.Randgridr
import me.kumatheta.feh.skill.weapon.SublimeSurge
import me.kumatheta.feh.skill.weapon.TrilemmaPlus

private fun nonStandardRefinableWeapons() = sequenceOf(
    "Flametongue+" to DragonC.basic(16) + DragonAdaptive,
    "Dark Breath+" to DragonC.basic(14) + sequenceOf(
        DragonAdaptive,
        aoeDebuff(
            2,
            true,
            Stat(atk = -7, spd = -7)
        )
    )
)

private val STANDARD_REFINABLE_WEAPONS = listOf(
    "Slaying Bow+" to BowC.basic(12) + SlayingEffect,
    "Slaying Lance+" to Lance.basic(14) + SlayingEffect,
    "Slaying Axe+" to Axe.basic(14) + SlayingEffect,
    "Slaying Edge+" to Sword.basic(14) + SlayingEffect,
    "Thoron+" to MagicB.basic(14),
    "Rexcailbur+" to MagicG.basic(14),
    "Silver Axe+" to Axe.basic(16),
    "Wo Gun+" to Axe.basic(14) + DealSpecialDamage,
    "Blarowl+" to MagicB.basic(10) + OwlEffect,
    "Gronnowl+" to MagicG.basic(10) + OwlEffect,
    "Shining Bow+" to BowC.basic(12) + ShiningEffect,
    "Barrier Lance+" to Lance.basic(14) + stance(res = 7),
    "Water Breath+" to DragonC.basic(14) + skillEffects(
        stance(def = 4, res = 4),
        DragonAdaptive
    ),
    "Armorsmasher+" to Sword.effective(14, MoveType.ARMORED),
    "Slaying Hammer+" to Axe.effective(14, MoveType.ARMORED),
    "Blarserpent+" to MagicB.basic(12) + rangeDefStat(def = 6, res = 6),
    "Guard Bow+" to BowC.basic(12) + rangeDefStat(def = 6, res = 6)
)

val FIXED_WEAPONS = sequenceOf(
    "Raudrblade+" to MagicR.bladeTome(13),
    "Brave Bow+" to BowC.brave(7),
    "Brave Sword+" to Sword.brave(8),
    "Brave Lance+" to Lance.brave(8),
    "Brave Axe+" to Axe.brave(8),
    "Gronnraven+" to MagicG.basic(11) + RavenBasic,
    "Emerald Axe+" to Axe.basic(12) + TriangleAdept(20),

    "Flametongue+" to DragonC.basic(15),
    "Silver Axe+" to Axe.basic(15),

    "Iron Lance" to Lance.basic(6),
    "Steel Lance" to Lance.basic(8),
    "Silver Lance" to Lance.basic(11),
    "Iron Sword" to Sword.basic(6),
    "Steel Sword" to Sword.basic(8),
    "Silver Sword" to Sword.basic(11),
    "Iron Axe" to Axe.basic(6),
    "Steel Axe" to Axe.basic(8),
    "Silver Axe" to Axe.basic(11),
    "Slaying Hammer" to Axe.effective(10, MoveType.ARMORED),
    "Steel Bow" to BowC.basic(6),
    "Assault" to Staff.basic(10),
    "Ridersbane+ Eff" to Lance.effective(14, MoveType.CAVALRY, neutralizeBonus = true, hp = 3),
    "Keen Gronnwolf+ Eff" to MagicG.effective(12, MoveType.CAVALRY, neutralizeBonus = true),
    "Armorsmasher+ Eff" to Sword.effective(14, MoveType.ARMORED, neutralizeBonus = true, hp = 3),
    "Zanbato+ Eff" to Sword.effective(14, MoveType.CAVALRY, neutralizeBonus = true, hp = 3),

    "Slow+" to Staff.basic(12) + aoeDebuff(
        range = 2,
        applyToTarget = true,
        debuff = Stat(spd = -7)
    ),
    "Gravity+" to Staff.basic(10) + aoeNegativeStatus(
        negativeStatus = NegativeStatus.GRAVITY,
        range = 1,
        applyToTarget = true
    ),

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

    "Trilemma+ Dazzling" to TrilemmaPlus + DisableCounter
)
val ALL_WEAPONS: SkillMap<BasicWeapon> = (FIXED_WEAPONS + STANDARD_REFINABLE_WEAPONS).toSkillMap()
val BASIC_REFINABLE_WEAPONS = (nonStandardRefinableWeapons() + STANDARD_REFINABLE_WEAPONS).toSkillMap()

fun weaponStat(might: Int, hp: Int = 0, spd: Int = 0, def: Int = 0, res: Int = 0): ExtraStat {
    return Stat(hp = hp, atk = might, spd = spd, def = def, res = res).toExtraStat()
}

fun WeaponType.basic(might: Int, hp: Int = 0, spd: Int = 0, def: Int = 0, res: Int = 0): BasicWeapon =
    BasicWeapon(
        this,
        weaponStat(might = might, hp = hp, spd = spd, def = def, res = res).toSkill()
    )

fun WeaponType.brave(might: Int) = basic(might, spd = -5) + BraveOnInit


operator fun BasicWeapon.plus(skillEffect: SkillEffect): BasicWeapon {
    return BasicWeapon(this.weaponType, this.skill.effects.asSequence().plus(skillEffect).toSkill())
}

operator fun BasicWeapon.plus(skillEffects: Sequence<SkillEffect>): BasicWeapon {
    return BasicWeapon(this.weaponType, this.skill.effects.asSequence().plus(skillEffects).toSkill())
}

fun BasicWeapon.getBasicRefine(statType: StatType): BasicWeapon {
    return this + if (this.weaponType.isRanged) {
        when (statType) {
            StatType.ATK -> Stat(hp = 2, atk = 1)
            StatType.SPD -> Stat(hp = 2, spd = 2)
            StatType.DEF -> Stat(hp = 2, def = 3)
            StatType.RES -> Stat(hp = 2, res = 3)
        }
    } else {
        when (statType) {
            StatType.ATK -> Stat(hp = 5, atk = 2)
            StatType.SPD -> Stat(hp = 5, spd = 3)
            StatType.DEF -> Stat(hp = 5, def = 4)
            StatType.RES -> Stat(hp = 5, res = 4)
        }
    }.toExtraStat()
}

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
        val neutralizeEffect = NeutralizeBonusBasic(ALL_STAT_TYPES)
        val effect = object : EffectOnFoe {
            override fun apply(combatStatus: CombatStatus<HeroUnit>): Sequence<SkillEffect> {
                return if (combatStatus.foe.moveType == moveType) {
                    sequenceOf(neutralizeEffect)
                } else {
                    emptySequence()
                }
            }
        }
        effect
    } else {
        null
    }
    val weaponStat = weaponStat(might = might, hp = hp, spd = spd, def = def, res = res)
    return BasicWeapon(
        this,
        sequenceOf(weaponStat, EffectiveAgainstMovement(moveType), foeEffect).filterNotNull().toSkill()
    )
}

private fun WeaponType.bladeTome(might: Int, hp: Int = 0, spd: Int = 0, def: Int = 0, res: Int = 0): BasicWeapon =
    BasicWeapon(
        this,
        sequenceOf(
            weaponStat(might = might, hp = hp, spd = spd, def = def, res = res),
            CoolDownCountAdjust(1),
            object : ExtraInCombatStatEffect {
                override fun apply(combatStatus: CombatStatus<InCombatStat>): Stat {
                    return Stat(atk = combatStatus.self.bonus.totalExceptHp)
                }
            }
        ).toSkill()
    )

