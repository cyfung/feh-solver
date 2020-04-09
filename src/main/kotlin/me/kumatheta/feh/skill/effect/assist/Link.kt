package me.kumatheta.feh.skill.effect.assist

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.Assist
import me.kumatheta.feh.skill.MovementAssist
import me.kumatheta.feh.skill.effect.AssistEffect

class Link(private val buff: Stat) : AssistEffect {
    override fun onAssist(
        battleState: BattleState,
        self: HeroUnit,
        ally: HeroUnit,
        assist: Assist,
        selfUseAssist: Boolean
    ) {
        if (assist is MovementAssist) {
            self.applyBuff(buff)
            ally.applyBuff(buff)
        }
    }

}