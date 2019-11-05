package com.bloombase.feh.skill.assist

import com.bloombase.feh.CombatResult
import com.bloombase.feh.HeroUnit
import com.bloombase.feh.NormalAssist
import com.bloombase.feh.WinLoss

abstract class Rally : NormalAssist() {
    override fun isValidPreCombat(self: HeroUnit, selfAttacks: List<CombatResult>): Boolean {
        return if (self.hasSpecialDebuff) {
            selfAttacks.all {
                it.potentialDamage < 5 && it.debuffSuccess == 0 && it.winLoss == WinLoss.LOSS
            }
        } else {
            selfAttacks.all {
                it.potentialDamage < 5 && it.debuffSuccess == 0
            }
        }
    }

}