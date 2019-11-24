package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.Passive
import me.kumatheta.feh.InCombatSkill
import me.kumatheta.feh.CombatSkillTrue

object DistantCounter : Passive {
    override val counterIgnoreRange: InCombatSkill<Boolean>?
        get() = CombatSkillTrue
}