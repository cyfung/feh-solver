package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.CombatStatus

interface CanCounter : SkillEffect, CombatStartEffect<BooleanAdjustment>

object DisableCounter : CanCounter {
    override fun apply(combatStatus: CombatStatus<HeroUnit>): BooleanAdjustment {
        return BooleanAdjustment.NEGATIVE
    }
}

object ReEnableCounter: CanCounter {
    override fun apply(combatStatus: CombatStatus<HeroUnit>): BooleanAdjustment {
        return BooleanAdjustment.POSITIVE
    }
}