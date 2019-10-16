package com.bloombase.feh

import com.bloombase.feh.skill.weapon.EmptyWeapon

interface Hero {
    val moveType: MoveType
    val isEmptyHanded: Boolean
    val weaponType: WeaponType
    val stat: Stat
    val cooldownCount: Int?
    val skillSet: SkillSet
}

fun Sequence<Skill>.plusIfNotNull(skill: Skill?): Sequence<Skill> {
    return if (skill == null) {
        this
    } else {
        this.plus(skill)
    }
}


open class HeroModel(
    override val moveType: MoveType,
    weapon: Weapon,
    special: Special?,
    stat: Stat,
    passives: List<Passive>,
    isFinalStat: Boolean = false
) : Hero {
    override val isEmptyHanded: Boolean = weapon is EmptyWeapon
    override val weaponType = weapon.weaponType
    final override val skillSet = SkillSet(passives.asSequence().plus(weapon).plusIfNotNull(special).toList())

    final override val stat: Stat = if (isFinalStat) {
        stat
    } else {
        skillSet.skills.asSequence().mapNotNull(Skill::extraStat).fold(stat) { sum, extraStat ->
            sum + extraStat
        }
    }

    final override val cooldownCount: Int? = if (special?.coolDownCount != null) {
        special.coolDownCount + skillSet.skills.sumBy(Skill::coolDownCountAdj)
    } else {
        null
    }

}