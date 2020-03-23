package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.MagicR
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.InCombatStat
import me.kumatheta.feh.skill.basic
import me.kumatheta.feh.skill.effect.InCombatStatEffect
import me.kumatheta.feh.skill.effect.PostCombatEffect
import me.kumatheta.feh.skill.effect.skillEffects
import me.kumatheta.feh.skill.plus

private val IN_COMBAT_BUFF = Stat(atk = 5, spd = 5)

private const val ID = "Ragnarok"

val Ragnarok = MagicR.basic(14) + skillEffects(
    object : InCombatStatEffect {
        override fun apply(combatStatus: CombatStatus<HeroUnit>): Stat {
            return if (combatStatus.self.currentHp == combatStatus.self.maxHp) {
                combatStatus.self.combatSkillData[ID] = true
                IN_COMBAT_BUFF
            } else {
                Stat.ZERO
            }
        }
    },
    object : PostCombatEffect {
        override fun onCombatEnd(combatStatus: CombatStatus<InCombatStat>, attacked: Boolean) {
            if (attacked) {
                if (combatStatus.self.heroUnit.combatSkillData[ID] == true) {
                    combatStatus.self.heroUnit.cachedEffect.takeNonLethalDamage(5)
                }
            }
        }
    }
)
