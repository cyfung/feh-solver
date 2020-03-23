package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat
import me.kumatheta.feh.foe
import me.kumatheta.feh.skill.Assist
import me.kumatheta.feh.skill.assist.BuffRelatedAssist
import me.kumatheta.feh.skill.effect.AssistEffect

object DefFeint3 : AssistEffect {
    override fun onAssist(
        battleState: BattleState,
        self: HeroUnit,
        ally: HeroUnit,
        assist: Assist,
        selfUseAssist: Boolean
    ) {
        if (assist !is BuffRelatedAssist) {
            return
        }
        battleState.unitsSeq(self.team.foe).filter {
            it.position.x == self.position.x || it.position.y == self.position.y
        }.forEach {
            it.applyDebuff(Stat(def = -7))
        }
    }
}