package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.CombatStatus

interface InCombatStatEffect : InCombatSkillEffect, CombatStartEffect<Stat>

class InCombatStatEffectBasic(private val stat: Stat) : InCombatStatEffect {
    override fun apply(combatStatus: CombatStatus<HeroUnit>): Stat {
        return stat
    }
}

