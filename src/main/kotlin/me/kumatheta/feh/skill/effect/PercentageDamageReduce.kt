package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.InCombatStat

interface PercentageDamageReduce : SkillEffect {
    fun getPercentageDamageReduce(combatStatus: CombatStatus<InCombatStat>, specialTriggered: Boolean): Int
}