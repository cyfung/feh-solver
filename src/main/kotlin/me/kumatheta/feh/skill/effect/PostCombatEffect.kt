package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.InCombatStat

interface PostCombatEffect: SkillEffect {
    fun onCombatEnd(combatStatus: CombatStatus<InCombatStat>, attacked: Boolean)
}