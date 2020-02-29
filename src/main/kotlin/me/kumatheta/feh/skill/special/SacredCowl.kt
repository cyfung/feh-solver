package me.kumatheta.feh.skill.special

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.DefenseSpecial
import me.kumatheta.feh.InCombatStat

object SacredCowl : DefenseSpecial(2) {
    override fun getReducedDamage(
        battleState: BattleState,
        self: InCombatStat,
        foe: InCombatStat,
        incomingDamage: Int
    ): Int? {
        return if (foe.heroUnit.weaponType.isRanged) {
            incomingDamage - incomingDamage * 3 / 10
        } else {
            null
        }
    }
}