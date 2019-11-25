package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.MapSkillMethod
import me.kumatheta.feh.NegativeStatus
import me.kumatheta.feh.Passive
import me.kumatheta.feh.foe

object PanicPloy3 : Passive {
    override val startOfTurn: MapSkillMethod<Unit>? = object : MapSkillMethod<Unit> {
        override fun apply(battleState: BattleState, self: HeroUnit) {
            battleState.unitsSeq(self.team.foe).filter {
                it.position.x == self.position.x || it.position.y == self.position.y
            }.filter {
                it.currentHp < self.currentHp
            }.forEach {
                it.addNegativeStatus(NegativeStatus.PANIC)
            }
        }
    }
}