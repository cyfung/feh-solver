package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.CombatStatus

interface CombatStartEffect<T> {
    fun apply(combatStatus: CombatStatus<HeroUnit>): T
}