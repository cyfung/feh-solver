package com.bloombase.feh.skill.passive

import com.bloombase.feh.*

object CloseCounter : Passive {
    override val ignoreRange: CombatSkillMethod<Boolean>?
        get() = CombatSkillMethodTrue
}