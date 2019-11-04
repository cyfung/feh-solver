package com.bloombase.feh.skill.assist

import com.bloombase.feh.AssistEffect
import com.bloombase.feh.CombatResult
import com.bloombase.feh.HeroUnit
import com.bloombase.feh.NormalAssist

abstract class Refresh : NormalAssist() {
    final override fun isValidPreCombat(
        self: HeroUnit,
        selfAttacks: List<CombatResult>?,
        possibleAttacks: Map<HeroUnit, List<CombatResult>>
    ): Boolean {
        return selfAttacks?.all {
            it.potentialDamage < 5
        } ?: true
    }

    override fun preCombatAssistEffect(self: HeroUnit, target: HeroUnit): AssistEffect {
        return AssistEffect(AssistEffect.Type.REFRESH)
    }

}

