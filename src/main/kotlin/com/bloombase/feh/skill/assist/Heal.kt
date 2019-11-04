package com.bloombase.feh.skill.assist

import com.bloombase.feh.AssistEffect
import com.bloombase.feh.CombatResult
import com.bloombase.feh.HeroUnit
import com.bloombase.feh.NormalAssist

abstract class Heal(private val threshold: Int) : NormalAssist() {
    override fun isValidPreCombat(
        self: HeroUnit,
        selfAttacks: List<CombatResult>?,
        possibleAttacks: Map<HeroUnit, List<CombatResult>>
    ): Boolean {
        return true
    }

    override fun preCombatAssistEffect(self: HeroUnit, target: HeroUnit): AssistEffect? {
        if (target.stat.hp - target.currentHp < threshold) {
            return null
        }
        return AssistEffect(AssistEffect.Type.HEAL)
    }
}