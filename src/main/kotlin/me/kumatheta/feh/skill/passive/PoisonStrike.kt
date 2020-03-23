package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.InCombatStat
import me.kumatheta.feh.skill.effect.PostCombatEffect
import me.kumatheta.feh.skill.effect.SpecialDebuff
import me.kumatheta.feh.skill.toSkill

fun poisonStrike(hpDamage: Int) = sequenceOf(
    SpecialDebuff.ALWAYS_AVAILABLE,
    object : PostCombatEffect {
        override fun onCombatEnd(combatStatus: CombatStatus<InCombatStat>, attacked: Boolean) {
            if (combatStatus.initAttack && !combatStatus.self.heroUnit.isDead) {
                combatStatus.foe.heroUnit.cachedEffect.takeNonLethalDamage(hpDamage)
            }
        }
    }
).toSkill()