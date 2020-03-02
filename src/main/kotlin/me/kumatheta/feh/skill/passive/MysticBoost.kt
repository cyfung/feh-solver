package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.combatStartSkillTrue
import me.kumatheta.feh.skill.inCombatSkillTrue

val MysticBoost3 = BasicSkill(
    denyAdaptiveDamage = combatStartSkillTrue,
    denyStaffAsNormal = inCombatSkillTrue,
    combatEnd = { combatStatus, _ ->
        combatStatus.self.heroUnit.cachedEffect.heal(6)
    }
)
