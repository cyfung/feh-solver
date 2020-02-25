package me.kumatheta.feh.skill.special

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.DamagingSpecial
import me.kumatheta.feh.InCombatStat
import me.kumatheta.feh.Stat

object Iceberg : DamagingSpecial(3) {
    override fun getDamage(battleState: BattleState, self: InCombatStat, foe: InCombatStat, defenderDefRes: Int): Int {
        return self.inCombatStat.res / 2
    }
}

object Bonfire : DamagingSpecial(3) {
    override fun getDamage(battleState: BattleState, self: InCombatStat, foe: InCombatStat, defenderDefRes: Int): Int {
        return self.inCombatStat.def / 2
    }
}

object DraconicAura : DamagingSpecial(3) {
    override fun getDamage(battleState: BattleState, self: InCombatStat, foe: InCombatStat, defenderDefRes: Int): Int {
        return self.inCombatStat.atk * 3 / 10
    }
}

class SelfStatBased(
    coolDownCount: Int,
    private val damageGetter: (Stat) -> Int
) : DamagingSpecial(coolDownCount) {
    override fun getDamage(battleState: BattleState, self: InCombatStat, foe: InCombatStat, defenderDefRes: Int): Int {
        return damageGetter(self.inCombatStat)
    }
}