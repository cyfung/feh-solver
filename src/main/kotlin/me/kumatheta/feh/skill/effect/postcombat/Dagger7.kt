package me.kumatheta.feh.skill.effect.postcombat

import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.InCombatStat
import me.kumatheta.feh.skill.aoeDebuffFoe
import me.kumatheta.feh.skill.effect.PostCombatEffect

private val DEBUFF = Stat(def = -7, res = -7)

val Dagger7Eff =  object : PostCombatEffect {
    override fun onCombatEnd(combatStatus: CombatStatus<InCombatStat>, attacked: Boolean) {
        if (attacked) {
            aoeDebuffFoe(combatStatus, DEBUFF)
        }
    }
}