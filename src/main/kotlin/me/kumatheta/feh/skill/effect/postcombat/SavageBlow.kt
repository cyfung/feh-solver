package me.kumatheta.feh.skill.effect.postcombat

import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.InCombatStat
import me.kumatheta.feh.skill.effect.PostCombatEffect
import me.kumatheta.feh.skill.nearbyAllies

class SavageBlow(private val damage: Int) : PostCombatEffect {
    override fun onCombatEnd(combatStatus: CombatStatus<InCombatStat>, attacked: Boolean) {
        if (attacked && !combatStatus.self.heroUnit.isDead) {
            combatStatus.foe.heroUnit.nearbyAllies(combatStatus.battleState, 2).forEach {
                it.cachedEffect.takeNonLethalDamage(damage)
            }
        }
    }
}