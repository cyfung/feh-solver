package me.kumatheta.feh.skill

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.MapSkillMethod
import me.kumatheta.feh.Stat
import me.kumatheta.feh.foe

inline fun ploy(
    crossinline f: (self: HeroUnit, foe: HeroUnit) -> Unit
): MapSkillMethod<Unit> = { battleState: BattleState, self: HeroUnit ->
    battleState.unitsSeq(self.team.foe).filter {
        self.inCardinalDirection(it)
    }.forEach {
        f(self, it)
    }
}

fun resBasedPloy3(stat: Stat): MapSkillMethod<Unit> {
    return ploy { self, foe ->
        if (foe.visibleStat.res < self.startOfTurnStat.res) {
            foe.applyDebuff(stat)
        }
    }
}