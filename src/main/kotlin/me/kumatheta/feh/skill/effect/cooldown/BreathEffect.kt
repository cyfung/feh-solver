package me.kumatheta.feh.skill.effect.cooldown

import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.CooldownChange
import me.kumatheta.feh.skill.InCombatStat
import me.kumatheta.feh.skill.NoCooldownChange
import me.kumatheta.feh.skill.effect.CoolDownChargeEffect

private val COOLDOWN_CHANGE = CooldownChange(1, 1)

object BreathEffect : CoolDownChargeEffect {
    override fun getAdjustment(combatStatus: CombatStatus<InCombatStat>): CooldownChange {
        return if (combatStatus.initAttack) {
            NoCooldownChange
        } else {
            COOLDOWN_CHANGE
        }
    }
}