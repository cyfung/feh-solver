package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.CombatStatus

interface Raven : CombatStartEffect<Boolean>, InCombatSkillEffect

object RavenBasic : Raven {
    override fun apply(combatStatus: CombatStatus<HeroUnit>): Boolean {
        return true
    }
}