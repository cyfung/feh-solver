package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.MapSkillMethod
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.allies

fun <R : Comparable<R>> opening(stat: Stat, selector: (HeroUnit) -> R): MapSkillMethod<Unit> =
    { battleState: BattleState, self: HeroUnit ->
        self.allies(battleState).maxBy(selector)?.applyBuff(stat)
    }