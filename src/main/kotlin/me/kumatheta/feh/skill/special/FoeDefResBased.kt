package me.kumatheta.feh.skill.special

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.DamagingSpecial
import me.kumatheta.feh.InCombatStat

class FoeDefResBased(
    coolDownCount: Int,
    private val multiplier: Int,
    private val divider: Int
) : DamagingSpecial(coolDownCount) {
    override fun getDamage(
        battleState: BattleState,
        self: InCombatStat,
        foe: InCombatStat,
        defenderDefRes: Int,
        atk: Int
    ): Int {
        return defenderDefRes * multiplier / divider
    }
}