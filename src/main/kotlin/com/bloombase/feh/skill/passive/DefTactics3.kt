package com.bloombase.feh.skill.passive

import com.bloombase.feh.*
import com.bloombase.feh.skill.Tactics

object DefTactics3 : Passive {
    override val startOfTurn: MapSkillMethod?
        get() = Tactics(Stat(def = 6))
}