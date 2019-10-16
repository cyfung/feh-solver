package com.bloombase.feh.skill.weapon

import com.bloombase.feh.*
import java.lang.IllegalStateException

object Fensalir : BasicWeapon(Lance, 16) {
    override val startOfTurn: MapSkillMethod?
        get() = object: MapSkillMethod {
            override fun apply(battleState: BattleState, self: HeroUnit) {
                val position = battleState.reverseMap[self]?: throw IllegalStateException()
                battleState.unitsAndPos(self.team.opponent).filter { it.value.distanceTo(position) <= 2 }.forEach {
                    it.key.applyDebuff(Stat(atk = -4))
                }
            }
        }
}