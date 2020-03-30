package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Lance
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.basic
import me.kumatheta.feh.skill.effect.DisableFollowUp
import me.kumatheta.feh.skill.effect.EffectOnFoe
import me.kumatheta.feh.skill.effect.FollowUpEffect
import me.kumatheta.feh.skill.effect.InCombatSkillEffect
import me.kumatheta.feh.skill.effect.InCombatStatEffect
import me.kumatheta.feh.skill.effect.SlayingEffect
import me.kumatheta.feh.skill.effect.incombatstat.FoxEffect
import me.kumatheta.feh.skill.effect.skillEffects
import me.kumatheta.feh.skill.plus

private val BUFF = Stat(atk = 4, spd = 4, def = 4, res = 4)

val ScytheOfSariel = Lance.basic(16) + skillEffects(
    SlayingEffect,
    object : InCombatStatEffect {
        override fun apply(combatStatus: CombatStatus<HeroUnit>): Stat {
            return if (combatStatus.foe.bonus.isNotZero() || combatStatus.foe.hasPositiveStatus) {
                BUFF
            } else {
                Stat.ZERO
            }
        }
    },
    object : EffectOnFoe {
        override fun apply(combatStatus: CombatStatus<HeroUnit>): Sequence<InCombatSkillEffect> {
            return if (combatStatus.foe.bonus.isNotZero() || combatStatus.foe.hasPositiveStatus) {
                sequenceOf(DisableFollowUp)
            } else {
                emptySequence()
            }
        }
    }
)