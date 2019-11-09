package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.*
import java.lang.IllegalStateException

object Fensalir : BasicWeapon(Lance, 16) {
    override val startOfTurn: MapSkillMethod<Unit>?
        get() = object: MapSkillMethod<Unit> {
            override fun apply(battleState: BattleState, self: HeroUnit) {
                val position = battleState.reverseMap[self]?: throw IllegalStateException()
                battleState.unitsAndPosSeq(self.team.opponent).filter { it.value.distanceTo(position) <= 2 }.forEach {
                    it.key.applyDebuff(Stat(atk = -4))
                }
            }
        }
}