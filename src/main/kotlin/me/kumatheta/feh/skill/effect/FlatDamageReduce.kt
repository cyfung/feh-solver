package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.InCombatStat

interface FlatDamageReduce : SkillEffect {
    fun getDamageReduce(combatStatus: CombatStatus<InCombatStat>, specialTriggered: Boolean): Int
}