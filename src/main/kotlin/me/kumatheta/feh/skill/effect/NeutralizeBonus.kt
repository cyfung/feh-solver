package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.StatType
import me.kumatheta.feh.skill.CombatStatus

interface NeutralizeBonus : InCombatSkillEffect, CombatStartEffect<Sequence<StatType>>

class NeutralizeBonusBasic(private val statTypes: Collection<StatType>) : NeutralizeBonus {
    override fun apply(combatStatus: CombatStatus<HeroUnit>): Sequence<StatType> {
        return statTypes.asSequence()
    }
}