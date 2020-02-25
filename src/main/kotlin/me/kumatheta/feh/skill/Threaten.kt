package me.kumatheta.feh.skill

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.MapSkillMethod
import me.kumatheta.feh.Stat
import me.kumatheta.feh.foe

fun threaten(stat: Stat): MapSkillMethod<Unit> = { battleState: BattleState, self: HeroUnit ->
    val position = self.position
    battleState.unitsSeq(self.team.foe).filter { it.position.distanceTo(position) <= 2 }.forEach {
        it.applyDebuff(Stat(atk = -4))
    }
}
