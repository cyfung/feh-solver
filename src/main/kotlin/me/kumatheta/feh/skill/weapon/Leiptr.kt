package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.BasicWeapon
import me.kumatheta.feh.InCombatSkill
import me.kumatheta.feh.Lance
import me.kumatheta.feh.inCombatSkillTrue

object Leiptr : BasicWeapon(Lance, 16) {
    override val counterIgnoreRange: InCombatSkill<Boolean>?
        get() = inCombatSkillTrue
}