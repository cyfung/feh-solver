package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.InCombatStat
import me.kumatheta.feh.skill.effect.DenyAdaptiveDamageEffectBasic
import me.kumatheta.feh.skill.effect.DenyStaffAsNormalBasic
import me.kumatheta.feh.skill.effect.PostCombatEffect
import me.kumatheta.feh.skill.toSkill

val MysticBoost3 = sequenceOf(
    DenyAdaptiveDamageEffectBasic,
    DenyStaffAsNormalBasic,
    object : PostCombatEffect {
        override fun onCombatEnd(combatStatus: CombatStatus<InCombatStat>, attacked: Boolean) {
            combatStatus.self.heroUnit.cachedEffect.heal(6)
        }
    }
).toSkill()
