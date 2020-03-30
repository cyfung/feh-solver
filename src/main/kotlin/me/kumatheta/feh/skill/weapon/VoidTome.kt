package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.MagicC
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.basic
import me.kumatheta.feh.skill.effect.BooleanAdjustment
import me.kumatheta.feh.skill.effect.FollowUpEffect
import me.kumatheta.feh.skill.effect.InCombatStatEffect
import me.kumatheta.feh.skill.effect.skillEffects
import me.kumatheta.feh.skill.plus

private val BUFF = Stat(atk = 5, def = 5, res = 5)

val VoidTome = MagicC.basic(17) + skillEffects(
    object : InCombatStatEffect {
        override fun apply(combatStatus: CombatStatus<HeroUnit>): Stat {
            return if (combatStatus.foe.visibleStat.atk >= 50 || combatStatus.foe.hasNegativeStatus || combatStatus.foe.penalty.isNotZero()) {
                BUFF
            } else {
                Stat.ZERO
            }
        }
    },
    object : FollowUpEffect {
        override fun apply(combatStatus: CombatStatus<HeroUnit>): BooleanAdjustment {
            return if (combatStatus.foe.visibleStat.spd >= 35 || combatStatus.foe.hasNegativeStatus || combatStatus.foe.penalty.isNotZero()) {
                BooleanAdjustment.POSITIVE
            } else {
                BooleanAdjustment.NEUTRAL
            }
        }
    }
)