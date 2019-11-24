package me.kumatheta.feh.skill.special

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.DamagingSpecial
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.InCombatStatus
import me.kumatheta.feh.Special

object Iceberg: DamagingSpecial(3) {
    override fun getDamage(battleState: BattleState, self: InCombatStatus, foe: InCombatStatus): Int {
        return self.inCombatStat.res / 2
    }
}