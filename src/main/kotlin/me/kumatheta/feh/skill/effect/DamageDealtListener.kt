package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.DamageDealt
import me.kumatheta.feh.skill.InCombatStat

interface DamageDealtListener : SkillEffect {
    fun onDamageDealt(combatStatus: CombatStatus<InCombatStat>, damageDealt: DamageDealt)
}