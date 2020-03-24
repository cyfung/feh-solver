package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.effect.BooleanAdjustment
import me.kumatheta.feh.skill.effect.DisableCounter
import me.kumatheta.feh.skill.effect.EffectOnFoe
import me.kumatheta.feh.skill.effect.FollowUpEffect
import me.kumatheta.feh.skill.effect.InCombatSkillEffect
import me.kumatheta.feh.skill.effect.SkillEffect
import me.kumatheta.feh.skill.effect.skillEffects
import me.kumatheta.feh.skill.toSkill

fun windsweep(minDiff: Int) = skillEffects(
    object : FollowUpEffect {
        override fun apply(combatStatus: CombatStatus<HeroUnit>): BooleanAdjustment {
            return if (combatStatus.initAttack) {
                BooleanAdjustment.NEGATIVE
            } else {
                BooleanAdjustment.NEUTRAL
            }
        }
    },
    object : EffectOnFoe {
        override fun apply(combatStatus: CombatStatus<HeroUnit>): Sequence<InCombatSkillEffect> {
            return if (combatStatus.initAttack && !combatStatus.foe.weaponType.targetRes && combatStatus.self.virtualSpd >= combatStatus.foe.virtualSpd + minDiff) {
                sequenceOf(DisableCounter)
            } else {
                emptySequence()
            }
        }
    }
).toSkill()