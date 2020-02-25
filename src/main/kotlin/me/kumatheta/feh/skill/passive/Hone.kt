package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.MapSkillMethod
import me.kumatheta.feh.Stat

fun hone(stat: Stat): MapSkillMethod<Unit> = { battleState: BattleState, self: HeroUnit ->
    battleState.unitsSeq(self.team).filterNot { it == self }
        .filter { it.position.distanceTo(self.position) == 1 }.forEach {
            it.applyBuff(stat)
        }
}