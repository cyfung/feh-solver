package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.CombatStatus

interface StaffAsNormal : CombatStartEffect<Boolean>, SkillEffect

object StaffAsNormalBasic : StaffAsNormal {
    override fun apply(combatStatus: CombatStatus<HeroUnit>): Boolean {
        return true
    }
}