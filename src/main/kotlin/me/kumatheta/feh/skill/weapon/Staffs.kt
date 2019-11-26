package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.BasicWeapon
import me.kumatheta.feh.BattleState
import me.kumatheta.feh.CombatEndSkill
import me.kumatheta.feh.InCombatStat
import me.kumatheta.feh.Staff
import me.kumatheta.feh.Stat

object SlowPlus : BasicWeapon(Staff, 12) {
    override val combatEnd: CombatEndSkill? = object : CombatEndSkill {
        override fun apply(
            battleState: BattleState,
            self: InCombatStat,
            foe: InCombatStat,
            attack: Boolean,
            attacked: Boolean
        ) {
            if (attacked) {
                battleState.unitsSeq(foe.heroUnit.team).filter { it.position.distanceTo(foe.heroUnit.position) <= 2 }
                    .forEach {
                        it.applyDebuff(Stat(spd = -7))
                    }
            }
        }
    }
}