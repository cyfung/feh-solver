package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.effect.BooleanAdjustment
import me.kumatheta.feh.skill.effect.DisableFollowUp
import me.kumatheta.feh.skill.effect.EffectOnFoe
import me.kumatheta.feh.skill.effect.FollowUpEffect
import me.kumatheta.feh.skill.effect.SkillEffect
import me.kumatheta.feh.skill.effect.skillEffects
import me.kumatheta.feh.skill.toSkill

fun waryFighter(percentageHp: Int) = skillEffects(
    object : FollowUpEffect {
        override fun apply(combatStatus: CombatStatus<HeroUnit>): BooleanAdjustment {
            return if (combatStatus.self.hpThreshold(percentageHp) >= 0) {
                BooleanAdjustment.NEGATIVE
            } else {
                BooleanAdjustment.NEUTRAL
            }
        }
    },
    object : EffectOnFoe {
        override fun apply(combatStatus: CombatStatus<HeroUnit>): Sequence<SkillEffect> {
            return if (combatStatus.self.hpThreshold(percentageHp) >= 0) {
                sequenceOf(DisableFollowUp)
            } else {
                emptySequence()
            }
        }
    }
).toSkill()