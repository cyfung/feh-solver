package me.kumatheta.feh.skill.effect.startofturn

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat
import me.kumatheta.feh.foe
import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.inCardinalDirection

inline fun ploy(
    crossinline f: (self: HeroUnit, foe: HeroUnit) -> Unit
) = BasicSkill(
    startOfTurn = { battleState: BattleState, self: HeroUnit ->
        battleState.unitsSeq(self.team.foe).filter {
            self.inCardinalDirection(it)
        }.forEach {
            f(self, it)
        }
    }
)

fun resBasedPloy3(stat: Stat) = ploy { self, foe ->
    if (foe.visibleStat.res < self.startOfTurnStat.res) {
        foe.applyDebuff(stat)
    }
}