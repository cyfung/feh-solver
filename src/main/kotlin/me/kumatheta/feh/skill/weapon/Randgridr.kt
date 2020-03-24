package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.ALL_STAT_TYPES
import me.kumatheta.feh.BowB
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.MoveType
import me.kumatheta.feh.Stat
import me.kumatheta.feh.StatType
import me.kumatheta.feh.skill.*
import me.kumatheta.feh.skill.effect.EffectOnFoe
import me.kumatheta.feh.skill.effect.InCombatSkillEffect
import me.kumatheta.feh.skill.effect.InCombatStatEffectBasic
import me.kumatheta.feh.skill.effect.NeutralizePenalty
import me.kumatheta.feh.skill.effect.SkillEffect
import me.kumatheta.feh.skill.effect.skillEffects

private val FOE_EFFECT = InCombatStatEffectBasic(Stat(atk = -6, def = -6))

val Randgridr = BowB.basic(17) + skillEffects(
    object : NeutralizePenalty {
        override fun apply(combatStatus: CombatStatus<HeroUnit>): Sequence<StatType> {
            return if (combatStatus.foe.currentHp == combatStatus.foe.maxHp) {
                ALL_STAT_TYPES.asSequence()
            } else {
                emptySequence()
            }
        }
    },
    object : EffectOnFoe {
        override fun apply(combatStatus: CombatStatus<HeroUnit>): Sequence<InCombatSkillEffect> {
            return if (combatStatus.foe.currentHp == combatStatus.foe.maxHp) {
                sequenceOf(FOE_EFFECT)
            } else {
                emptySequence()
            }
        }
    }
)