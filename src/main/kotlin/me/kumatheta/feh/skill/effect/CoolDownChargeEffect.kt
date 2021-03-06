package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.CooldownChange
import me.kumatheta.feh.skill.InCombatStat

interface CoolDownChargeEffect : InCombatSkillEffect {
    fun getAdjustment(combatStatus: CombatStatus<InCombatStat>): CooldownChange
}