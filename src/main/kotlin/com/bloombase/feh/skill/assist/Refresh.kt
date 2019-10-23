package com.bloombase.feh.skill.assist

import com.bloombase.feh.Assist
import com.bloombase.feh.CombatResult
import com.bloombase.feh.HeroUnit

abstract class Refresh : Assist(Type.REFRESH) {
    override fun isValidPreCombat(
        self: HeroUnit,
        selfAttacks: List<CombatResult>?,
        possibleAttacks: Map<HeroUnit, List<CombatResult>>
    ): Boolean {
        return selfAttacks.orEmpty().all {
            it.damageDealt < 5
        }
    }

    override fun isValidPreCombatTarget(self: HeroUnit, target: HeroUnit): Boolean {
        return !target.available
    }
}