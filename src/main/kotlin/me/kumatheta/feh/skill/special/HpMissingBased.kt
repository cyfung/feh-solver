package me.kumatheta.feh.skill.special

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.skill.DamagingSpecial
import me.kumatheta.feh.skill.InCombatStat

open class HpMissingBased(
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
        return (self.heroUnit.maxHp - self.heroUnit.currentHp) * multiplier / divider
    }
}