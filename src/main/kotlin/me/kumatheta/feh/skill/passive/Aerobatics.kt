package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.MapSkillMethod
import me.kumatheta.feh.MoveType
import me.kumatheta.feh.Passive
import me.kumatheta.feh.Position
import me.kumatheta.feh.util.surroundings

object Aerobatics3 : Passive {
    override val teleport: MapSkillMethod<Sequence<Position>>? = object : MapSkillMethod<Sequence<Position>> {
        override fun apply(battleState: BattleState, self: HeroUnit): Sequence<Position> {
            return battleState.unitsSeq(self.team).filter { it != self }.filter {
                when (it.moveType) {
                    MoveType.INFANTRY, MoveType.CAVALRY, MoveType.ARMORED -> true
                    else -> false
                }
            }.flatMap {
                it.position.surroundings(battleState.maxPosition)
            }
        }
    }
}