package me.kumatheta.feh.skill.special

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.DamagingSpecial
import me.kumatheta.feh.InCombatStat

object Luna : DamagingSpecial(3) {
    override fun getDamage(battleState: BattleState, self: InCombatStat, foe: InCombatStat, defenderDefRes: Int): Int {
        return defenderDefRes / 2
    }
}

object Moonbow : DamagingSpecial(3) {
    override fun getDamage(battleState: BattleState, self: InCombatStat, foe: InCombatStat, defenderDefRes: Int): Int {
        return defenderDefRes * 3 / 10
    }
}