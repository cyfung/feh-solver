package me.kumatheta.feh.skill.special

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.DamageDealt
import me.kumatheta.feh.skill.DamagingSpecial
import me.kumatheta.feh.skill.InCombatStat
import me.kumatheta.feh.skill.effect.DamageDealtListener
import me.kumatheta.feh.skill.inCombatVirtualSpd

object Sirius : DamagingSpecial(2, object : DamageDealtListener {
    override fun onDamageDealt(combatStatus: CombatStatus<InCombatStat>, damageDealt: DamageDealt) {
        if (damageDealt.attackSpecialTriggered) {
            combatStatus.self.heroUnit.heal(damageDealt.damage * 3 / 10)
        }
    }
}) {
    override fun getDamage(
        battleState: BattleState,
        self: InCombatStat,
        foe: InCombatStat,
        defenderDefRes: Int,
        atk: Int
    ): Int {
        return self.inCombatVirtualSpd * 3 / 10
    }
}


