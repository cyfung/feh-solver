package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.DaggerC
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.InCombatStat
import me.kumatheta.feh.skill.aoeDebuffFoe
import me.kumatheta.feh.skill.basic
import me.kumatheta.feh.skill.effect.DisableCounter
import me.kumatheta.feh.skill.effect.DisableFollowUp
import me.kumatheta.feh.skill.effect.EffectOnFoe
import me.kumatheta.feh.skill.effect.InCombatSkillEffect
import me.kumatheta.feh.skill.effect.InCombatStatEffect
import me.kumatheta.feh.skill.effect.PostCombatEffect
import me.kumatheta.feh.skill.effect.SkillEffect
import me.kumatheta.feh.skill.effect.skillEffects
import me.kumatheta.feh.skill.plus

private const val ID = "Lyfjaberg"

private val BUFF = Stat(atk = 4, spd = 4)

val Lyfjaberg = DaggerC.basic(14, res = 3) + skillEffects(
    object : InCombatStatEffect {
        override fun apply(combatStatus: CombatStatus<HeroUnit>): Stat {
            return if (combatStatus.self.hpThreshold(50) >= 0) {
                combatStatus.self.combatSkillData[ID] = true
                BUFF
            } else {
                Stat.ZERO
            }
        }
    },
    object : EffectOnFoe {
        override fun apply(combatStatus: CombatStatus<HeroUnit>): Sequence<InCombatSkillEffect> {
            return if (combatStatus.initAttack && combatStatus.self.hpThreshold(50) >= 0) {
                sequenceOf(DisableFollowUp)
            } else {
                emptySequence()
            }
        }
    },
    object : PostCombatEffect {
        override fun onCombatEnd(combatStatus: CombatStatus<InCombatStat>, attacked: Boolean) {
            if (attacked) {
                aoeDebuffFoe(combatStatus, Stat(def = -7, res = -7))
                if (combatStatus.self.heroUnit.combatSkillData[ID] == true) {
                    combatStatus.self.heroUnit.cachedEffect.takeNonLethalDamage(4)
                }
            }
        }
    }
)
