package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.skill.effect.SpecialDebuff
import me.kumatheta.feh.skill.BasicSkill

fun poisonStrike(hpDamage:Int) = BasicSkill(
    specialDebuff = SpecialDebuff.ALWAYS_AVAILABLE,
    combatEnd = { combatStatus, _ ->
        if (combatStatus.initAttack && !combatStatus.self.heroUnit.isDead) {
            combatStatus.foe.heroUnit.cachedEffect.takeNonLethalDamage(hpDamage)
        }
    }
)
