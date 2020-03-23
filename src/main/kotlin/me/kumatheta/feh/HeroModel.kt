package me.kumatheta.feh

import me.kumatheta.feh.skill.Assist
import me.kumatheta.feh.skill.Passive
import me.kumatheta.feh.skill.Skill
import me.kumatheta.feh.skill.SkillSet
import me.kumatheta.feh.skill.Special
import me.kumatheta.feh.skill.Weapon
import me.kumatheta.feh.skill.effect.Debuffer
import me.kumatheta.feh.skill.effect.EffectiveAgainstMovement
import me.kumatheta.feh.skill.effect.EffectiveAgainstWeapon
import me.kumatheta.feh.skill.effect.ExtraStat
import me.kumatheta.feh.skill.effect.NeutralizeEffectiveAgainstMovement
import me.kumatheta.feh.skill.effect.NeutralizeEffectiveAgainstWeapon
import me.kumatheta.feh.skill.effect.PhantomStat
import me.kumatheta.feh.skill.effect.CoolDownCountAdjust
import me.kumatheta.feh.skill.effect.SpecialDebuff
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
}

fun Sequence<Skill>.plusIfNotNull(skill: Skill?): Sequence<Skill> {
    return if (skill == null) {
        this
    } else {
        this.plus(skill)
    }
}

open class HeroModel(
    override val name: String,
    override val imageName: String,
    final override val group: Int?,
    val engageDelay: Int?,
    final override val moveType: MoveType,
    val weapon: Weapon,
    final override val assist: Assist?,
    final override val special: Special?,
    stat: Stat,
    passives: List<Passive>,
    isFinalStat: Boolean = false
) : Hero {
    final override val isEmptyHanded: Boolean = weapon is EmptyWeapon
    final override val weaponType = weapon.weaponType
    final override val skillSet =
        SkillSet(
            passives.asSequence().plus(weapon).plusIfNotNull(assist).plusIfNotNull(
                special
            ).flatMap { it.effects.asSequence() }
        )
    final override val debuffer = skillSet.get<Debuffer>().any()
    final override val specialDebuffer = skillSet.get<SpecialDebuff>().maxBy {
        if (it == SpecialDebuff.ALWAYS_AVAILABLE) {
            0
        } else {
            1
        }
    }

    final override val effectiveAgainstMoveType = skillSet.get<EffectiveAgainstMovement>().map { it.moveType }.toSet()
    final override val effectiveAgainstWeaponType = skillSet.get<EffectiveAgainstWeapon>().map { it.weaponType }.toSet()
    final override val neutralizeEffectiveMoveType =
        skillSet.get<NeutralizeEffectiveAgainstMovement>().map { it.moveType }.contains(moveType)
    final override val neutralizeEffectiveWeaponType =
        skillSet.get<NeutralizeEffectiveAgainstWeapon>().map { it.weaponType }.contains(weaponType)

    final override val baseStat: Stat = if (isFinalStat) {
        stat
    } else {
        skillSet.get<ExtraStat>().fold(stat) { sum, extraStat ->
            sum + extraStat.value
        }
    }

    final override val phantomStat: Stat =
        skillSet.get<PhantomStat>().fold(Stat.ZERO) { sum, stat ->
            sum + stat.value
        }

    override val cooldownCount: Int? = if (special?.coolDownCount != null) {
        special.coolDownCount + skillSet.get<CoolDownCountAdjust>().sumBy { it.value }
    } else {
        null
    }
}
