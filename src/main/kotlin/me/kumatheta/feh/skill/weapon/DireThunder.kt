package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.BasicWeapon
import me.kumatheta.feh.InCombatSkill
import me.kumatheta.feh.MagicB
import me.kumatheta.feh.inCombatSkillTrue

object DireThunder : BasicWeapon(MagicB, 9) {
    override val brave: InCombatSkill<Boolean>?
        get() = inCombatSkillTrue
}