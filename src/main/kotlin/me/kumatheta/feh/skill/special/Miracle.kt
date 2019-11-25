package me.kumatheta.feh.skill.special

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.DefenseSpecial
import me.kumatheta.feh.InCombatStat

object Miracle : DefenseSpecial(5) {
    override fun getReducedDamage(
        battleState: BattleState,
        self: InCombatStat,
        foe: InCombatStat,
        incomingDamage: Int
    ): Int? {
        throw UnsupportedOperationException()
    }
}