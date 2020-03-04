package me.kumatheta.feh.skill.effect.startofturn

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.MoveType
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.adjacentAllies

fun hone(buff: Stat, moveType: MoveType? = null) =
    if (moveType == null) {
        BasicSkill(
            startOfTurn = { battleState: BattleState, self: HeroUnit ->
                self.adjacentAllies(battleState).forEach {
                    it.applyBuff(buff)
                }
            }
        )
    } else {
        BasicSkill(
            startOfTurn = { battleState: BattleState, self: HeroUnit ->
                self.adjacentAllies(battleState).filter { it.moveType == moveType }.forEach {
                    it.applyBuff(buff)
                }
            }
        )
    }