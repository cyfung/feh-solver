package me.kumatheta.feh.skill.effect.postcombat

import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.InCombatStat
import me.kumatheta.feh.skill.effect.PostCombatEffect

class Seal(private val stat: Stat) : PostCombatEffect {
    override fun onCombatEnd(combatStatus: CombatStatus<InCombatStat>, attacked: Boolean) {
        if (!combatStatus.self.heroUnit.isDead && !combatStatus.foe.heroUnit.isDead) {
            combatStatus.foe.heroUnit.cachedEffect.applyDebuff(stat)
        }
    }
}