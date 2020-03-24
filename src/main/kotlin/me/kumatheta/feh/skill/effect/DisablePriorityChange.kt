package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.CombatStatus

interface DisablePriorityChange : CombatStartEffect<Boolean>, InCombatSkillEffect

object DisablePriorityChangeBasic : DisablePriorityChange {
    override fun apply(combatStatus: CombatStatus<HeroUnit>): Boolean {
        return true
    }
}