package me.kumatheta.feh.skill

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.WeaponType
import me.kumatheta.feh.skill.effect.BooleanAdjustment
import me.kumatheta.feh.skill.effect.DisableFollowUp
import me.kumatheta.feh.skill.effect.EffectOnFoe
import me.kumatheta.feh.skill.effect.FollowUpEffect
import me.kumatheta.feh.skill.effect.SkillEffect

fun breaker(weaponType: WeaponType, percentage: Int) = sequenceOf<SkillEffect>(
    object : FollowUpEffect {
        override fun apply(combatStatus: CombatStatus<HeroUnit>): BooleanAdjustment {
            return if (combatStatus.foe.weaponType == weaponType && combatStatus.self.hpThreshold(percentage) >= 0) {
                BooleanAdjustment.POSITIVE
            } else {
                BooleanAdjustment.NEUTRAL
            }
        }
    },
    object : EffectOnFoe {
        override fun apply(combatStatus: CombatStatus<HeroUnit>): Sequence<SkillEffect> {
            return if (combatStatus.foe.weaponType == weaponType && combatStatus.self.hpThreshold(percentage) >= 0) {
                sequenceOf(DisableFollowUp)
            } else {
                emptySequence()
            }
        }
    }
).toSkill()