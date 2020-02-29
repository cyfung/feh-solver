package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.CombatEndSkill
import me.kumatheta.feh.Passive
import me.kumatheta.feh.SpecialDebuff

object PoisonStrike3 : Passive {
    override val specialDebuff: SpecialDebuff = SpecialDebuff.ONLY_WHEN_ALIVE

    override val combatEnd: CombatEndSkill? = { combatStatus, _ ->
        if (combatStatus.initAttack && !combatStatus.self.heroUnit.isDead) {
            combatStatus.foe.heroUnit.cachedEffect.takeNonLethalDamage(10)
        }
    }
}
