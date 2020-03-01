package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.skill.*

object MysticBoost3 : BasicSkill() {
    override val denyAdaptiveDamage: MapSkillMethod<Boolean>? = { _, _ ->
        true
    }
    override val denyStaffAsNormal: InCombatSkill<Boolean>?
        get() = inCombatSkillTrue
    override val combatEnd: CombatEndSkill? = { combatStatus, _ ->
        combatStatus.self.heroUnit.cachedEffect.heal(6)
    }
}
