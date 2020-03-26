package me.kumatheta.feh

import me.kumatheta.feh.skill.Assist
import me.kumatheta.feh.skill.Passive
import me.kumatheta.feh.skill.Skill
import me.kumatheta.feh.skill.SkillSet
import me.kumatheta.feh.skill.Special
import me.kumatheta.feh.skill.Weapon
import me.kumatheta.feh.skill.effect.CoolDownCountAdjust
import me.kumatheta.feh.skill.effect.Debuffer
import me.kumatheta.feh.skill.effect.EffectiveAgainstMovement
import me.kumatheta.feh.skill.effect.EffectiveAgainstWeapon
import me.kumatheta.feh.skill.effect.ExtraStat
import me.kumatheta.feh.skill.effect.NeutralizeEffectiveAgainstMovement
import me.kumatheta.feh.skill.effect.NeutralizeEffectiveAgainstWeapon
import me.kumatheta.feh.skill.effect.PhantomStat
import me.kumatheta.feh.skill.effect.PostInitiateMovement
import me.kumatheta.feh.skill.effect.SkillEffect
import me.kumatheta.feh.skill.effect.SpecialDebuff
import me.kumatheta.feh.skill.effect.Transform
import me.kumatheta.feh.skill.weapon.EmptyWeapon

interface Hero {
    val debuffer: Boolean
    val moveType: MoveType
    val isEmptyHanded: Boolean
    val weaponType: WeaponType
    val assist: Assist?
    val special: Special?
    val skillSet: SkillSet
    val baseStat: Stat
    val group: Int?
    val specialDebuffer: SpecialDebuff?
    val effectiveAgainstMoveType: Set<MoveType>
    val effectiveAgainstWeaponType: Set<WeaponType>
    val neutralizeEffectiveMoveType: Boolean
    val neutralizeEffectiveWeaponType: Boolean
    val name: String
    val imageName: String
    val cooldownCount: Int?
    val phantomStat: Stat
    val transform: Transform?
}

fun Sequence<Skill>.plusIfNotNull(skill: Skill?): Sequence<Skill> {
    return if (skill == null) {
        this
    } else {
        this.plus(skill)
    }
}

class HeroModel private constructor(
    override val name: String,
    override val imageName: String,
    override val group: Int?,
    val engageDelay: Int?,
    override val moveType: MoveType,
    val weapon: Weapon,
    override val assist: Assist?,
    override val special: Special?,
    stat: Stat,
    isFinalStat: Boolean,
    skillEffects: List<SkillEffect>
) : Hero {

    constructor(
        name: String,
        imageName: String,
        group: Int?,
        engageDelay: Int?,
        moveType: MoveType,
        weapon: Weapon,
        assist: Assist?,
        special: Special?,
        stat: Stat,
        passives: List<Passive>,
        isFinalStat: Boolean = false
    ) : this(
        name, imageName, group, engageDelay, moveType, weapon, assist, special, stat, isFinalStat,
        passives.asSequence().plus(weapon).plusIfNotNull(assist)
            .plusIfNotNull(special).flatMap { it.effects.asSequence() }.toList()
    )

    override val isEmptyHanded: Boolean = weapon is EmptyWeapon
    override val weaponType = weapon.weaponType
    override val skillSet = SkillSet(skillEffects)
    override val debuffer = skillEffects.filterIsInstance<Debuffer>().any()
    override val specialDebuffer = skillEffects.filterIsInstance<SpecialDebuff>().maxBy {
        if (it == SpecialDebuff.ALWAYS_AVAILABLE) {
            0
        } else {
            1
        }
    }

    override val transform: Transform?
    init {
        val transform = skillEffects.filterIsInstance<Transform>()
        if (transform.size > 1) {
            throw IllegalStateException("more than one transform")
        }
        this.transform = transform.singleOrNull()
    }

    override val effectiveAgainstMoveType = skillEffects.filterIsInstance<EffectiveAgainstMovement>().map { it.moveType }.toSet()
    override val effectiveAgainstWeaponType = skillEffects.filterIsInstance<EffectiveAgainstWeapon>().map { it.weaponType }.toSet()
    override val neutralizeEffectiveMoveType =
        skillEffects.filterIsInstance<NeutralizeEffectiveAgainstMovement>().map { it.moveType }.contains(moveType)
    override val neutralizeEffectiveWeaponType =
        skillEffects.filterIsInstance<NeutralizeEffectiveAgainstWeapon>().map { it.weaponType }.contains(weaponType)

    override val baseStat: Stat = if (isFinalStat) {
        stat
    } else {
        skillEffects.filterIsInstance<ExtraStat>().fold(stat) { sum, extraStat ->
            sum + extraStat.value
        }
    }

    override val phantomStat: Stat =
        skillEffects.filterIsInstance<PhantomStat>().fold(Stat.ZERO) { sum, stat ->
            sum + stat.value
        }

    override val cooldownCount: Int? = if (special?.coolDownCount != null) {
        special.coolDownCount + skillEffects.filterIsInstance<CoolDownCountAdjust>().sumBy { it.value }
    } else {
        null
    }
}
