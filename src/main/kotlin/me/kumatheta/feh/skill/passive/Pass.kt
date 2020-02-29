package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.MapSkillMethod
import me.kumatheta.feh.Passive

class Pass(percentageHp: Int) : Passive {
    override val pass: MapSkillMethod<Boolean>? = { _, self ->
        self.hpThreshold(percentageHp) >= 0
    }
}