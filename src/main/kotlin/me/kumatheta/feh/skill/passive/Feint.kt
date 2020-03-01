package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat
import me.kumatheta.feh.foe
import me.kumatheta.feh.skill.Assist
import me.kumatheta.feh.skill.AssistRelated
import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.assist.BuffRelatedAssist

val DefFeint3 = BasicSkill(assistRelated = object : AssistRelated {
    override fun apply(
        battleState: BattleState,
        self: HeroUnit,
        ally: HeroUnit,
        assist: Assist,
        useAssist: Boolean
    ) {
        if (assist !is BuffRelatedAssist) {
            return
        }
        battleState.unitsSeq(self.team.foe).filter {
            it.position.x == self.position.x || it.position.y == self.position.y ||
                    it.position.x == ally.position.x || it.position.y == ally.position.y
        }.forEach {
            it.applyDebuff(Stat(def = -7))
        }
    }
})