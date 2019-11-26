package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.MapSkillMethod
import me.kumatheta.feh.Passive
import me.kumatheta.feh.Stat

object HoneAtk3 : Passive {
    override val startOfTurn: MapSkillMethod<Unit>? = object : MapSkillMethod<Unit> {
        override fun apply(battleState: BattleState, self: HeroUnit) {
            battleState.unitsSeq(self.team).filterNot { it == self }
                .filter { it.position.distanceTo(self.position) == 1 }.forEach {
                it.applyBuff(Stat(atk = 4))
            }
        }
    }
}