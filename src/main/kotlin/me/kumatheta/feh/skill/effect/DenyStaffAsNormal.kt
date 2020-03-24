package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.CombatStatus

interface DenyStaffAsNormal : CombatStartEffect<Boolean>, InCombatSkillEffect

object DenyStaffAsNormalBasic : DenyStaffAsNormal {
    override fun apply(combatStatus: CombatStatus<HeroUnit>): Boolean {
        return true
    }
}