package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.CombatStartSkill
import me.kumatheta.feh.Passive
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.BlowOrStance

object SwiftSparrow2 : Passive {
    override val inCombatStat: CombatStartSkill<Stat>? = BlowOrStance(Stat(atk = 4, spd = 4), Stat.ZERO)
}

