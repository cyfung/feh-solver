package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.CombatStatus

interface FollowUpEffect : SkillEffect, CombatStartEffect<BooleanAdjustment>

object DisableFollowUp: FollowUpEffect {
    override fun apply(combatStatus: CombatStatus<HeroUnit>): BooleanAdjustment {
        return BooleanAdjustment.NEGATIVE
    }
}
object ReEnableFollowUp: FollowUpEffect {
    override fun apply(combatStatus: CombatStatus<HeroUnit>): BooleanAdjustment {
        return BooleanAdjustment.POSITIVE
    }
}