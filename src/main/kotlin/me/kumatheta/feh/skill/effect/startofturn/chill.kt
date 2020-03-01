package me.kumatheta.feh.skill.effect.startofturn

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat
import me.kumatheta.feh.foe
import me.kumatheta.feh.skill.BasicSkill

inline fun <R : Comparable<R>> chill(debuff: Stat, crossinline criteria: (HeroUnit) -> R) = BasicSkill(
    startOfTurn = { battleState: BattleState, self: HeroUnit ->
        battleState.unitsSeq(self.team.foe).maxBy {
            criteria(it)
        }?.applyDebuff(debuff)
    }
)