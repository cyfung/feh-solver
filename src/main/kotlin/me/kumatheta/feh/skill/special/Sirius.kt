package me.kumatheta.feh.skill.special

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.skill.DamageDealt
import me.kumatheta.feh.skill.DamagingSpecial
import me.kumatheta.feh.skill.InCombatStat
import me.kumatheta.feh.skill.PerAttackListener

object Sirius : DamagingSpecial(2) {
    override val damageDealtListener: PerAttackListener<DamageDealt>? = { combatStatus, damageDealt ->
        if (damageDealt.attackSpecialTriggered) {
            combatStatus.self.heroUnit.heal(damageDealt.damage * 3 / 10)
        }
    }


    override fun getDamage(battleState: BattleState, self: InCombatStat, foe: InCombatStat, defenderDefRes: Int, atk: Int): Int {
        return self.inCombatStat.spd * 3 / 10
    }

}


