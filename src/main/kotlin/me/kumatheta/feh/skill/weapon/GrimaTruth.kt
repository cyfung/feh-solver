package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.MagicR
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.InCombatStat
import me.kumatheta.feh.skill.aoeBuffAlly
import me.kumatheta.feh.skill.aoeDebuffFoe
import me.kumatheta.feh.skill.basic
import me.kumatheta.feh.skill.effect.PostCombatEffect
import me.kumatheta.feh.skill.plus

val GrimaTruth = MagicR.basic(14, def = 3) + object : PostCombatEffect {
    override fun onCombatEnd(combatStatus: CombatStatus<InCombatStat>, attacked: Boolean) {
        if (attacked) {
            aoeDebuffFoe(combatStatus, Stat(atk = -5, spd = -5))
            aoeBuffAlly(combatStatus, Stat(atk = 5, spd = 5))
        }
    }
}