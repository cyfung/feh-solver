package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.CombatEndSkill
import me.kumatheta.feh.Passive
import me.kumatheta.feh.Stat

object Fury3 : Passive {
    override val extraStat: Stat? = Stat(atk = 3, spd = 3, def = 3, res = 3)

    override val combatEnd: CombatEndSkill? = { combatStatus, _ ->
        combatStatus.self.heroUnit.endOfCombatEffects.takeNonLethalDamage(6)
    }
}