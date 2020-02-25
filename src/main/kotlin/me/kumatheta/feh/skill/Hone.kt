package me.kumatheta.feh.skill

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.MapSkillMethod
import me.kumatheta.feh.Stat

fun hone(stat: Stat): MapSkillMethod<Unit> = { battleState: BattleState, self: HeroUnit ->
    self.adjacentAllies(battleState).forEach {
        it.applyBuff(stat)
    }
}
