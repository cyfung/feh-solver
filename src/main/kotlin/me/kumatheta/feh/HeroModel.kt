package me.kumatheta.feh

import me.kumatheta.feh.skill.weapon.EmptyWeapon

interface Hero {
    val debuffer: Boolean
    val moveType: MoveType
    val isEmptyHanded: Boolean
    val weaponType: WeaponType
    val assist: Assist?
    val special: Special?
    val skillSet: SkillSet
    val stat: Stat
    val group: Int?
    val hasSpecialDebuff: Boolean
    val effectiveAgainstMoveType: Set<MoveType>
    val effectiveAgainstWeaponType: Set<WeaponType>
    val neutralizeEffectiveMoveType: Boolean
    val neutralizeEffectiveWeaponType: Boolean
    val name: String
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
        SkillSet(passives.asSequence().plus(weapon).plusIfNotNull(assist).plusIfNotNull(special).toList())
    final override val debuffer: Boolean = skillSet.skills.any { it.debuffer }
    final override val hasSpecialDebuff: Boolean = skillSet.skills.any { it.hasSpecialDebuff }
    final override val effectiveAgainstMoveType = skillSet.groupAsSet(Skill::effectiveAgainstMoveType)
    final override val effectiveAgainstWeaponType = skillSet.groupAsSet(Skill::effectiveAgainstWeaponType)
    final override val neutralizeEffectiveMoveType = skillSet.groupAsSet(Skill::neutralizeEffectiveMoveType).contains(moveType)
    final override val neutralizeEffectiveWeaponType = skillSet.groupAsSet(Skill::neutralizeEffectiveWeaponType).contains(weaponType)

    final override val stat: Stat = if (isFinalStat) {
        stat
    } else {
        skillSet.skills.asSequence().mapNotNull(Skill::extraStat).fold(stat) { sum, extraStat ->
            sum + extraStat
        }
    }

    val cooldownCount: Int? = if (special?.coolDownCount != null) {
        special.coolDownCount + skillSet.skills.sumBy(Skill::coolDownCountAdj)
    } else {
        null
    }
}