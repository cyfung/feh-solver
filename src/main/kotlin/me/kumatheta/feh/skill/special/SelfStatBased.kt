package me.kumatheta.feh.skill.special

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.skill.DamagingSpecial
import me.kumatheta.feh.skill.InCombatStat
import me.kumatheta.feh.Stat

class SelfStatBased(
    coolDownCount: Int,
    private val damageGetter: (Stat) -> Int
) : DamagingSpecial(coolDownCount) {
    override fun getDamage(
        battleState: BattleState,
        self: InCombatStat,
        foe: InCombatStat,
        defenderDefRes: Int,
        atk: Int
    ): Int {
        return damageGetter(self.inCombatStat)
    }
}