package me.kumatheta.feh.skill.effect.others

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.effect.BraveEffect

object BraveOnInit : BraveEffect {
    override fun apply(combatStatus: CombatStatus<HeroUnit>): Boolean {
        return combatStatus.initAttack
    }
}