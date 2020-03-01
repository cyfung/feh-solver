package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.Lance
import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.BasicWeapon
import me.kumatheta.feh.skill.inCombatSkillTrue
import me.kumatheta.feh.skill.weaponStat

val Leiptr = BasicWeapon(
    Lance, BasicSkill(
        extraStat = weaponStat(16),
        counterIgnoreRange = inCombatSkillTrue
    )
)