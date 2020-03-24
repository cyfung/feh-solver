package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.DragonC
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.basic
import me.kumatheta.feh.skill.effect.DisableFollowUp
import me.kumatheta.feh.skill.effect.EffectOnFoe
import me.kumatheta.feh.skill.effect.InCombatSkillEffect
import me.kumatheta.feh.skill.effect.InCombatStatEffect
import me.kumatheta.feh.skill.effect.SkillEffect
import me.kumatheta.feh.skill.effect.others.DragonAdaptive
import me.kumatheta.feh.skill.effect.skillEffects
import me.kumatheta.feh.skill.plus

private val BUFF = Stat(atk = 6, res = 6)

val FellBreath = DragonC.basic(19) + skillEffects(
    DragonAdaptive,
    object : InCombatStatEffect {
        override fun apply(combatStatus: CombatStatus<HeroUnit>): Stat {
            return if (combatStatus.foe.currentHp < combatStatus.foe.maxHp) {
                BUFF
            } else {
                Stat.ZERO
            }
        }
    },
    object : EffectOnFoe {
        override fun apply(combatStatus: CombatStatus<HeroUnit>): Sequence<InCombatSkillEffect> {
            return if (combatStatus.foe.currentHp < combatStatus.foe.maxHp) {
                sequenceOf(DisableFollowUp)
            } else {
                emptySequence()
            }
        }
    }
)