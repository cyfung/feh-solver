package com.bloombase.feh.skill.assist

import com.bloombase.feh.AssistEffect
import com.bloombase.feh.CombatResult
import com.bloombase.feh.HeroUnit
import com.bloombase.feh.NormalAssist

private const val HEAL_AMOUNT = 10

object ReciprocalAid : NormalAssist() {
    override fun isValidPreCombat(
        self: HeroUnit,
        selfAttacks: List<CombatResult>?,
        possibleAttacks: Map<HeroUnit, List<CombatResult>>
    ): Boolean {
        return self.isEmptyHanded
    }

    override fun preCombatAssistEffect(self: HeroUnit, target: HeroUnit): AssistEffect? {
        return if (
            target.stat.hp > target.currentHp &&
            self.currentHp > target.currentHp &&
            target.stat.hp >= self.currentHp &&
            self.stat.hp >= target.currentHp
        ) {
            AssistEffect(AssistEffect.Type.DONOR_HEAL)
        } else {
            null
        }
    }
}