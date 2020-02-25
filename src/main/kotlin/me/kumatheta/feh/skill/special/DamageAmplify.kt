package me.kumatheta.feh.skill.special

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.DamagingSpecial
import me.kumatheta.feh.InCombatStat

class DamageAmplify(coolDownCount: Int, private val percentage: Int) : DamagingSpecial(coolDownCount) {
    override fun getDamage(
        battleState: BattleState,
        self: InCombatStat,
        foe: InCombatStat,
        defenderDefRes: Int,
        atk: Int
    ): Int {
        return (atk - defenderDefRes) * percentage / 100
    }
}