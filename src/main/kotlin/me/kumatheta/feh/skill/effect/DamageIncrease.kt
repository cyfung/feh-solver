package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.InCombatStat

interface DamageIncrease : SkillEffect {
    fun getDamageIncrease(combatStatus: CombatStatus<InCombatStat>, specialTriggered: Boolean): Int
}