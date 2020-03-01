package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.Sword
import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.BasicWeapon
import me.kumatheta.feh.skill.inCombatSkillTrue
import me.kumatheta.feh.skill.weaponStat

val Ragnell = BasicWeapon(
    Sword, BasicSkill(
        extraStat = weaponStat(16),
        counterIgnoreRange = inCombatSkillTrue
    )
)