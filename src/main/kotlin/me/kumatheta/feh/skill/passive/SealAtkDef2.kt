package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.Passive
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.seal

object SealAtkDef2 : Passive {
    override val combatEnd
        get() = seal(Stat(def = -5, atk = -5))
}

