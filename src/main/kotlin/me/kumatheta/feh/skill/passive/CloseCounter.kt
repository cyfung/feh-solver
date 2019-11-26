package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.InCombatSkill
import me.kumatheta.feh.Passive
import me.kumatheta.feh.inCombatSkillTrue

object CloseCounter : Passive {
    override val counterIgnoreRange: InCombatSkill<Boolean>?
        get() = inCombatSkillTrue
}