package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.Passive
import me.kumatheta.feh.CombatSkillMethod
import me.kumatheta.feh.CombatSkillMethodTrue

object DistantCounter : Passive {
    override val counterIgnoreRange: CombatSkillMethod<Boolean>?
        get() = CombatSkillMethodTrue
}