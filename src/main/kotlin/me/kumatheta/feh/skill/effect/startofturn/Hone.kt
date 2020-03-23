package me.kumatheta.feh.skill.effect.startofturn

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.MoveType
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.adjacentAllies
import me.kumatheta.feh.skill.effect.StartOfTurnEffect

fun hone(buff: Stat, moveType: MoveType? = null) =
    if (moveType == null) {
        object : StartOfTurnEffect {
            override fun onStartOfTurn(battleState: BattleState, self: HeroUnit) {
                self.adjacentAllies(battleState).forEach {
                    it.applyBuff(buff)
                }
            }
        }
    } else {
        object : StartOfTurnEffect {
            override fun onStartOfTurn(battleState: BattleState, self: HeroUnit) {
                self.adjacentAllies(battleState).filter { it.moveType == moveType }.forEach {
                    it.applyBuff(buff)
                }
            }
        }
    }

fun hone(atk: Int = 0,
                 spd: Int = 0,
                 def: Int = 0,
                 res: Int = 0,
         moveType: MoveType? = null
) = hone(Stat(atk = atk, spd = spd, def = def, res = res), moveType = moveType)