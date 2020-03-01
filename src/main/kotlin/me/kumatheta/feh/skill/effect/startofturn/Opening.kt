package me.kumatheta.feh.skill.effect.startofturn

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.allies

inline fun <R : Comparable<R>> opening(stat: Stat, crossinline selector: (HeroUnit) -> R) = BasicSkill(
    startOfTurn = { battleState: BattleState, self: HeroUnit ->
        self.allies(battleState).maxBy(selector)?.applyBuff(stat)
    }
)