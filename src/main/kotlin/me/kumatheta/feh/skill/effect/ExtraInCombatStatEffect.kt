package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.InCombatStat

interface ExtraInCombatStatEffect : InCombatSkillEffect {
    fun apply(combatStatus: CombatStatus<InCombatStat>): Stat
}