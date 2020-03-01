package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.skill.CombatEndSkill
import me.kumatheta.feh.skill.Passive
import me.kumatheta.feh.SpecialDebuff
import me.kumatheta.feh.skill.BasicSkill

object PoisonStrike3 : BasicSkill() {
    override val specialDebuff: SpecialDebuff = SpecialDebuff.ALWAYS_AVAILABLE

    override val combatEnd: CombatEndSkill? = { combatStatus, _ ->
        if (combatStatus.initAttack && !combatStatus.self.heroUnit.isDead) {
            combatStatus.foe.heroUnit.cachedEffect.takeNonLethalDamage(10)
        }
    }
}
