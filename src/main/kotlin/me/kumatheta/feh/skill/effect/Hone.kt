package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.MapSkillMethod
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.adjacentAllies

fun hone(stat: Stat): MapSkillMethod<Unit> = { battleState: BattleState, self: HeroUnit ->
    self.adjacentAllies(battleState).forEach {
        it.applyBuff(stat)
    }
}
