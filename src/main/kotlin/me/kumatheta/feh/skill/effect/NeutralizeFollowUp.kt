package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.CombatStatus

interface NeutralizeFollowUp : InCombatSkillEffect, CombatStartEffect<BooleanAdjustment>

object NeutralizeGuaranteeFollowUp : NeutralizeFollowUp {
    override fun apply(combatStatus: CombatStatus<HeroUnit>): BooleanAdjustment {
        return BooleanAdjustment.POSITIVE
    }
}

object NeutralizePreventFollowUp : NeutralizeFollowUp {
    override fun apply(combatStatus: CombatStatus<HeroUnit>): BooleanAdjustment {
        return BooleanAdjustment.NEGATIVE
    }
}