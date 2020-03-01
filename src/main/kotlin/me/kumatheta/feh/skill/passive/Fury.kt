package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.BasicSkill

val Fury3 = BasicSkill(
    extraStat = Stat(atk = 3, spd = 3, def = 3, res = 3),
    combatEnd = { combatStatus, _ ->
        combatStatus.self.heroUnit.cachedEffect.takeNonLethalDamage(6)
    }
)