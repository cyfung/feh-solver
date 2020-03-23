package me.kumatheta.feh.skill.special

import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.DamageDealt
import me.kumatheta.feh.skill.InCombatStat
import me.kumatheta.feh.skill.effect.DamageDealtListener

class Aether(coolDownCount: Int) : FoeDefResBased(coolDownCount, 1, 2, object : DamageDealtListener {
    override fun onDamageDealt(combatStatus: CombatStatus<InCombatStat>, damageDealt: DamageDealt) {
        if (damageDealt.attackSpecialTriggered) {
            combatStatus.self.heroUnit.heal(damageDealt.damage / 2)
        }
    }
})
