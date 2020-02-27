package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.InCombatSkill
import me.kumatheta.feh.Passive
import me.kumatheta.feh.inCombatSkillTrue

object WrathfulStaff3 : Passive {
    override val staffAsNormal: InCombatSkill<Boolean>? = inCombatSkillTrue
}