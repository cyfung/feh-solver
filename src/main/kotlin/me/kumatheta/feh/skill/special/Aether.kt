package me.kumatheta.feh.skill.special

import me.kumatheta.feh.DamageDealt
import me.kumatheta.feh.PerAttackListener

class Aether(coolDownCount: Int) : FoeDefResBased(coolDownCount, 1, 2) {
    override val damageDealtListener: PerAttackListener<DamageDealt>? = { combatStatus, damageDealt ->
        if (damageDealt.attackSpecialTriggered) {
            combatStatus.self.heroUnit.heal(damageDealt.damage / 2)
        }
    }
}