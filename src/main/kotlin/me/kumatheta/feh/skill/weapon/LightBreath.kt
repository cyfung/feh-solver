package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.DragonC
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.InCombatStat
import me.kumatheta.feh.skill.adjacentAllies
import me.kumatheta.feh.skill.basic
import me.kumatheta.feh.skill.effect.PostCombatEffect
import me.kumatheta.feh.skill.plus

private val BUFF = Stat(def=4, res=4)

val LightBreathPlus = DragonC.basic(13) + object : PostCombatEffect {
    override fun onCombatEnd(combatStatus: CombatStatus<InCombatStat>, attacked: Boolean) {
        if (combatStatus.initAttack) {
            combatStatus.self.heroUnit.adjacentAllies(combatStatus.battleState).forEach {
                it.cachedEffect.applyBuff(BUFF)
            }
        }
    }
}