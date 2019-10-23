package com.bloombase.feh.skill.assist

import com.bloombase.feh.Assist
import com.bloombase.feh.CombatResult
import com.bloombase.feh.HeroUnit
import com.bloombase.feh.WinLoss

abstract class Heal : Assist(Type.HEAL) {
    override fun isValidPreCombat(
        self: HeroUnit,
        selfAttacks: List<CombatResult>?,
        possibleAttacks: Map<HeroUnit, List<CombatResult>>
    ): Boolean {
        return true
    }
}