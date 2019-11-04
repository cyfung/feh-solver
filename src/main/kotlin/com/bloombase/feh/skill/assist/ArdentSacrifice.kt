package com.bloombase.feh.skill.assist

import com.bloombase.feh.AssistEffect
import com.bloombase.feh.CombatResult
import com.bloombase.feh.HeroUnit
import com.bloombase.feh.NormalAssist

private const val HEAL_AMOUNT = 10

object ArdentSacrifice: NormalAssist() {
    override fun isValidPreCombat(
        self: HeroUnit,
        selfAttacks: List<CombatResult>?,
        possibleAttacks: Map<HeroUnit, List<CombatResult>>
    ): Boolean {
        return self.isEmptyHanded && self.currentHp > HEAL_AMOUNT
    }

    override fun preCombatAssistEffect(self: HeroUnit, target: HeroUnit): AssistEffect? {
        if (target.stat.hp - target.currentHp < HEAL_AMOUNT) {
            return null
        }
        return AssistEffect(AssistEffect.Type.DONOR_HEAL)
    }
}