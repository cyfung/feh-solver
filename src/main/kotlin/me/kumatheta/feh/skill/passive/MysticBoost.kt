package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.CombatEndSkill
import me.kumatheta.feh.InCombatSkill
import me.kumatheta.feh.MapSkillMethod
import me.kumatheta.feh.Passive
import me.kumatheta.feh.inCombatSkillTrue

object MysticBoost3 : Passive {
    override val denyAdaptiveDamage: MapSkillMethod<Boolean>? = { _, _ ->
        true
    }
    override val denyStaffAsNormal: InCombatSkill<Boolean>?
        get() = inCombatSkillTrue
    override val combatEnd: CombatEndSkill? = { combatStatus, _ ->
        combatStatus.self.heroUnit.endOfCombatEffects.heal(6)
    }
}
