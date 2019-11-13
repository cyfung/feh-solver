package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.BasicWeapon
import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Lance
import me.kumatheta.feh.MapSkillMethod
import me.kumatheta.feh.Stat
import me.kumatheta.feh.foe

object Fensalir : BasicWeapon(Lance, 16) {
    override val startOfTurn: MapSkillMethod<Unit>?
        get() = object : MapSkillMethod<Unit> {
            override fun apply(battleState: BattleState, self: HeroUnit) {
                val position = self.position
                battleState.unitsSeq(self.team.foe).filter { it.position.distanceTo(position) <= 2 }.forEach {
                    it.applyDebuff(Stat(atk = -4))
                }
            }
        }
}