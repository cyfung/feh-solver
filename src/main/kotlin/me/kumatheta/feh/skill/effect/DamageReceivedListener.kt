package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.DamageDealt
import me.kumatheta.feh.skill.InCombatStat

interface DamageReceivedListener : InCombatSkillEffect {
    fun onDamageReceived(combatStatus: CombatStatus<InCombatStat>, damageReceived: DamageDealt)
}