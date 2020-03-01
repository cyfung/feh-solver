package me.kumatheta.feh.skill.effect.startofturn

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat
import me.kumatheta.feh.foe
import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.util.surroundings

fun sabotage(stat: Stat) = BasicSkill(
    startOfTurn = { battleState: BattleState, self: HeroUnit ->
        val threshold = self.startOfTurnStat.res - 3
        battleState.unitsSeq(self.team.foe).filter { it.visibleStat.res <= threshold }.filter { foe ->
            foe.position.surroundings(battleState.maxPosition).any {
                val heroUnit = battleState.getChessPiece(it) as? HeroUnit ?: return@any false
                heroUnit.team == foe.team
            }
        }.forEach {
            it.applyDebuff(stat)
        }
    }
)