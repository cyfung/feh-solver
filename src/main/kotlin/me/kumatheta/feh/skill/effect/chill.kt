package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.MapSkillMethod
import me.kumatheta.feh.Stat
import me.kumatheta.feh.foe

inline fun <R : Comparable<R>> chill(debuff: Stat, crossinline criteria: (HeroUnit) -> R): MapSkillMethod<Unit> = { battleState: BattleState, self: HeroUnit ->
    battleState.unitsSeq(self.team.foe).maxBy {
        criteria(it)
    }?.applyDebuff(debuff)
}