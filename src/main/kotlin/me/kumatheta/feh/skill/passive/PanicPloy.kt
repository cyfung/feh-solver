package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.MapSkillMethod
import me.kumatheta.feh.NegativeStatus
import me.kumatheta.feh.Passive
import me.kumatheta.feh.skill.Ploy

object PanicPloy3 : Passive {
    override val startOfTurn: MapSkillMethod<Unit>? = Ploy({ self, foe ->
        foe.currentHp < self.currentHp
    }) {
        it.addNegativeStatus(NegativeStatus.PANIC)
    }

}

