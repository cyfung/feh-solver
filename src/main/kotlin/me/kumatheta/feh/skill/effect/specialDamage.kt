package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.InCombatStat

val dealSpecialDamage = BasicSkill(
    damageIncrease = { _: CombatStatus<InCombatStat>, specialTriggered: Boolean ->
        if (specialTriggered) {
            10
        } else {
            0
        }
    }
)