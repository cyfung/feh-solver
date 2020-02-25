package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.MapSkillMethod
import me.kumatheta.feh.Passive
import me.kumatheta.feh.Stat
import me.kumatheta.feh.foe
import me.kumatheta.feh.util.surroundings

fun sabotage(stat: Stat): MapSkillMethod<Unit> = { battleState: BattleState, self: HeroUnit ->
    val threshold = self.stat.res - 3
    battleState.unitsSeq(self.team.foe).filter { it.stat.res <= threshold }.filter { foe ->
        foe.position.surroundings(battleState.maxPosition).any {
            val heroUnit = battleState.getChessPiece(it) as? HeroUnit ?: return@any false
            heroUnit.team == foe.team
        }
    }.forEach {
        it.applyDebuff(stat)
    }
}