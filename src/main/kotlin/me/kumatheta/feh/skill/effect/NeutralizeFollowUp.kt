package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.CombatStatus

interface NeutralizeFollowUp : InCombatSkillEffect, CombatStartEffect<Boolean>

object NeutralizeFollowUpBasic : NeutralizeFollowUp {
    override fun apply(combatStatus: CombatStatus<HeroUnit>): Boolean {
        return true
    }
}