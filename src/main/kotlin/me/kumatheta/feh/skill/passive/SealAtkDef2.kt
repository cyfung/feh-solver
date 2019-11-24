package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.*
import me.kumatheta.feh.skill.Seal

object SealAtkDef2: Passive {
    override val combatEnd
        get() = Seal(Stat(def = -5, atk = -5))
}

