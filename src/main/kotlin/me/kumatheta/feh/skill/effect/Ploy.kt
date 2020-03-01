package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.MapSkillMethod
import me.kumatheta.feh.Stat
import me.kumatheta.feh.foe
import me.kumatheta.feh.skill.inCardinalDirection

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