package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.skill.Passive
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.effect.seal

object SealAtkDef2 : BasicSkill() {
    override val combatEnd
        get() = seal(Stat(def = -5, atk = -5))
}

