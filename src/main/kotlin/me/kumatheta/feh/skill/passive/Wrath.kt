package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.skill.*
import me.kumatheta.feh.skill.effect.wrathDamage
import me.kumatheta.feh.skill.effect.wrathSpecialCharge

fun wrath(hpPercentage: Int)= BasicSkill(
    startOfTurn = wrathSpecialCharge(hpPercentage),
    damageIncrease = wrathDamage(hpPercentage)
)