package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.*

object CloseCounter : Passive {
    override val ignoreRange: CombatSkillMethod<Boolean>?
        get() = CombatSkillMethodTrue
}