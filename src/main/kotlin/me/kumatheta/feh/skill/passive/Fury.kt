package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.skill.CombatEndSkill
import me.kumatheta.feh.skill.Passive
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.BasicSkill

object Fury3 : BasicSkill() {
    override val extraStat: Stat? = Stat(atk = 3, spd = 3, def = 3, res = 3)

    override val combatEnd: CombatEndSkill? = { combatStatus, _ ->
        combatStatus.self.heroUnit.cachedEffect.takeNonLethalDamage(6)
    }
}