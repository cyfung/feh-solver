package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.*

object PanicPloy3 : Passive {
    override val startOfTurn: MapSkillMethod<Unit>? = object : MapSkillMethod<Unit> {
        override fun apply(battleState: BattleState, self: HeroUnit) {
            battleState.unitsSeq(self.team.foe).filter {
                it.currentHp < self.currentHp
            }.forEach {
                it.addNegativeStatus(NegativeStatus.PANIC)
            }
        }
    }
}