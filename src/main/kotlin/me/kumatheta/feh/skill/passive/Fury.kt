package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.InCombatStat
import me.kumatheta.feh.skill.effect.PostCombatEffect
import me.kumatheta.feh.skill.toExtraStat
import me.kumatheta.feh.skill.toSkill

fun fury(bonusStat: Int, nonLethalDamage: Int = bonusStat*2) = sequenceOf(
    Stat(atk = bonusStat, spd = bonusStat, def = bonusStat, res = bonusStat).toExtraStat(),
    object : PostCombatEffect {
        override fun onCombatEnd(combatStatus: CombatStatus<InCombatStat>, attacked: Boolean) {
            combatStatus.self.heroUnit.cachedEffect.takeNonLethalDamage(nonLethalDamage)
        }
    }
).toSkill()