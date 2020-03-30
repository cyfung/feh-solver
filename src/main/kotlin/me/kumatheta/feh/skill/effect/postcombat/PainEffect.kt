package me.kumatheta.feh.skill.effect.postcombat

import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.InCombatStat
import me.kumatheta.feh.skill.effect.PostCombatEffect

object PainEffect : PostCombatEffect {
    override fun onCombatEnd(combatStatus: CombatStatus<InCombatStat>, attacked: Boolean) {
        if (attacked) {
            combatStatus.foe.heroUnit.cachedEffect.takeNonLethalDamage(10)
        }
    }
}