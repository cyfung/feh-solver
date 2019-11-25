package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.MapSkillMethod
import me.kumatheta.feh.Passive
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.Ploy

object ResPloy3 : Passive {
    override val startOfTurn: MapSkillMethod<Unit>? = Ploy({ self, foe ->
        foe.stat.res < self.stat.res
    }) {
        it.applyDebuff(Stat(res = -5))
    }
}