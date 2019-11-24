package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.*

object CloseCounter : Passive {
    override val counterIgnoreRange: InCombatSkill<Boolean>?
        get() = CombatSkillTrue
}