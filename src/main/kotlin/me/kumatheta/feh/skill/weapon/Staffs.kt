package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.BasicWeapon
import me.kumatheta.feh.CombatEndSkill
import me.kumatheta.feh.Staff
import me.kumatheta.feh.Stat

object SlowPlus : BasicWeapon(Staff, 12) {
    override val combatEnd: CombatEndSkill? = { combatStatus, attacked ->
        if (attacked) {
            combatStatus.battleState.unitsSeq(combatStatus.foe.heroUnit.team)
                .filter { it.position.distanceTo(combatStatus.foe.heroUnit.position) <= 2 }
                .forEach {
                    it.applyDebuff(Stat(spd = -7))
                }
        }
    }
}
