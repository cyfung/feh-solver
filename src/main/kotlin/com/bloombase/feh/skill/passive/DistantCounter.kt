package com.bloombase.feh.skill.passive

import com.bloombase.feh.Passive
import com.bloombase.feh.CombatSkillMethod
import com.bloombase.feh.CombatSkillMethodTrue

object DistantCounter : Passive {
    override val ignoreRange: CombatSkillMethod<Boolean>?
        get() = CombatSkillMethodTrue
}