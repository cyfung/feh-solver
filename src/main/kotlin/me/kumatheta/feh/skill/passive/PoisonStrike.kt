package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.CombatEndSkill
import me.kumatheta.feh.Passive

object PoisonStrike3 : Passive {
    // FIXME only has special debuff in not die
    override val specialDebuff: Boolean
        get() = true

    override val combatEnd: CombatEndSkill? = { combatStatus, _ ->
        if (combatStatus.initAttack && !combatStatus.self.heroUnit.isDead) {
            combatStatus.foe.heroUnit.cachedEffect.takeNonLethalDamage(10)
        }
    }
}
