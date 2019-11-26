package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.MapSkillMethod
import me.kumatheta.feh.Passive
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.Tactics

object SpdTactics3 : Passive {
    override val startOfTurn: MapSkillMethod<Unit>?
        get() = Tactics(Stat(spd = 6))
}

object DefTactics3 : Passive {
    override val startOfTurn: MapSkillMethod<Unit>?
        get() = Tactics(Stat(def = 6))
}

object AtkTactics3 : Passive {
    override val startOfTurn: MapSkillMethod<Unit>?
        get() = Tactics(Stat(atk = 6))
}