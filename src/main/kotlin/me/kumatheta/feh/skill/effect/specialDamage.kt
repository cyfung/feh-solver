package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.CombatStatus
import me.kumatheta.feh.InCombatStat

val specialDamage: ((CombatStatus<InCombatStat>, specialTriggered: Boolean) -> Int)? = { _, specialTriggered ->
    if (specialTriggered) {
        10
    } else {
        0
    }
}