package me.kumatheta.feh.skill.effect.followup

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.effect.BooleanAdjustment
import me.kumatheta.feh.skill.effect.EffectOnFoe
import me.kumatheta.feh.skill.effect.EffectOnFoeBasic
import me.kumatheta.feh.skill.effect.FollowUpEffect
import me.kumatheta.feh.skill.effect.NeutralizeFollowUp
import me.kumatheta.feh.skill.effect.NeutralizeGuaranteeFollowUp
import me.kumatheta.feh.skill.effect.NeutralizePreventFollowUp
import me.kumatheta.feh.skill.effect.SkillEffect
import me.kumatheta.feh.skill.effect.skillEffects
import me.kumatheta.feh.skill.toSkill

val NullFollowUp3 = skillEffects(
    NeutralizePreventFollowUp,
    EffectOnFoeBasic(listOf(NeutralizeGuaranteeFollowUp))
).toSkill()