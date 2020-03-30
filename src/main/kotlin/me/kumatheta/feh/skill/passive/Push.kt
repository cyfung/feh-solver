package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat
import me.kumatheta.feh.StatType
import me.kumatheta.feh.contains
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.InCombatStat
import me.kumatheta.feh.skill.Skill
import me.kumatheta.feh.skill.effect.InCombatStatEffect
import me.kumatheta.feh.skill.effect.PostCombatEffect
import me.kumatheta.feh.skill.effect.skillEffects
import me.kumatheta.feh.skill.toSkill
import me.kumatheta.feh.statPairSequence
import me.kumatheta.feh.toStat

fun push4(id: String, buff: Stat) = skillEffects(
    object : InCombatStatEffect {
        override fun apply(combatStatus: CombatStatus<HeroUnit>): Stat {
            return if (combatStatus.self.hpThreshold(25) >= 0) {
                combatStatus.self.combatSkillData[id] = true
                buff
            } else {
                Stat.ZERO
            }
        }
    },
    object : PostCombatEffect {
        override fun onCombatEnd(combatStatus: CombatStatus<InCombatStat>, attacked: Boolean) {
            if (attacked) {
                if (combatStatus.self.heroUnit.combatSkillData[id] == true) {
                    combatStatus.self.heroUnit.cachedEffect.takeNonLethalDamage(5)
                }
            }
        }
    }
).toSkill()

fun allPush4() = statPairSequence {
    val name = "${it.first}/${it.second} Push 4"
    name to push4(name, it.toStat(7))
}