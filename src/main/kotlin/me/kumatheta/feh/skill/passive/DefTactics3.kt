package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.*
import me.kumatheta.feh.skill.Tactics

object DefTactics3 : Passive {
    override val startOfTurn: MapSkillMethod<Unit>?
        get() = Tactics(Stat(def = 6))
}