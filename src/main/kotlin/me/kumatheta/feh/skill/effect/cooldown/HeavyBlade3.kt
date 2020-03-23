package me.kumatheta.feh.skill.effect.cooldown

import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.CooldownChange
import me.kumatheta.feh.skill.InCombatStat
import me.kumatheta.feh.skill.NoCooldownChange
import me.kumatheta.feh.skill.effect.CoolDownChargeEffect

private val COOLDOWN_CHANGE = CooldownChange(1, 0)

object HeavyBlade3 : CoolDownChargeEffect {
    override fun getAdjustment(combatStatus: CombatStatus<InCombatStat>): CooldownChange {
        return if (combatStatus.self.inCombatStat.atk > combatStatus.foe.inCombatStat.atk) {
            COOLDOWN_CHANGE
        } else {
            NoCooldownChange
        }
    }
}