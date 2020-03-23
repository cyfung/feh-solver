package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.CooldownChange
import me.kumatheta.feh.skill.InCombatStat
import me.kumatheta.feh.skill.NoCooldownChange
import me.kumatheta.feh.skill.effect.BooleanAdjustment
import me.kumatheta.feh.skill.effect.CoolDownChargeEffect
import me.kumatheta.feh.skill.effect.FollowUpEffect
import me.kumatheta.feh.skill.toSkill

private val COOLDOWN_CHANGE = CooldownChange(1, 0)

val BoldFighter3 = sequenceOf(
    object : FollowUpEffect {
        override fun apply(combatStatus: CombatStatus<HeroUnit>): BooleanAdjustment {
            return if (combatStatus.initAttack) {
                BooleanAdjustment.POSITIVE
            } else {
                BooleanAdjustment.NEUTRAL
            }
        }
    },
    object : CoolDownChargeEffect {
        override fun getAdjustment(combatStatus: CombatStatus<InCombatStat>): CooldownChange {
            return if (combatStatus.initAttack) {
                COOLDOWN_CHANGE
            } else {
                NoCooldownChange
            }
        }
    }
).toSkill()