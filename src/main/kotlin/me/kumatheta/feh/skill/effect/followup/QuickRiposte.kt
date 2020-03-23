package me.kumatheta.feh.skill.effect.followup

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.effect.BooleanAdjustment
import me.kumatheta.feh.skill.effect.FollowUpEffect

class QuickRiposte(private val percentage: Int) : FollowUpEffect {
    override fun apply(combatStatus: CombatStatus<HeroUnit>): BooleanAdjustment {
        return if (!combatStatus.initAttack && combatStatus.self.hpThreshold(percentage) >= 0) {
            BooleanAdjustment.POSITIVE
        } else {
            BooleanAdjustment.NEUTRAL
        }
    }
}