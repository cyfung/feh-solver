package com.bloombase.feh.skill.passive

import com.bloombase.feh.*
import com.bloombase.feh.skill.Seal

object SealAtkDef2: Passive {
    override val postCombat
        get() = Seal(Stat(def = -5, atk = -5))
}

