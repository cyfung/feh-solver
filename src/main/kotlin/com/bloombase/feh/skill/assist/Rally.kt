package com.bloombase.feh.skill.assist

import com.bloombase.feh.Assist
import com.bloombase.feh.CombatResult
import com.bloombase.feh.HeroUnit
import com.bloombase.feh.WinLoss

abstract class Rally : Assist(Type.RALLY) {
    override fun isValidPreCombat(
        self: HeroUnit,
        selfAttacks: List<CombatResult>?,
        possibleAttacks: Map<HeroUnit, List<CombatResult>>
    ): Boolean {
        if (selfAttacks == null) {
            return true
        }
        return if (self.hasSpecialDebuff) {
            selfAttacks.all {
                it.damageDealt < 5 && it.debuffSuccess > 0 && it.winLoss == WinLoss.LOSS
            }
        } else {
            selfAttacks.all {
                it.damageDealt < 5 && it.debuffSuccess > 0
            }
        }
    }

}