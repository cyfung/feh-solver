package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.BasicWeapon
import me.kumatheta.feh.InCombatSkill
import me.kumatheta.feh.Sword
import me.kumatheta.feh.inCombatSkillTrue

object Ragnell : BasicWeapon(Sword, 16) {
    override val counterIgnoreRange: InCombatSkill<Boolean>?
        get() = inCombatSkillTrue
}