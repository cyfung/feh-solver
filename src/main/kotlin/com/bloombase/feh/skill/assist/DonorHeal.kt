package com.bloombase.feh.skill.assist

import com.bloombase.feh.Assist
import com.bloombase.feh.CombatResult
import com.bloombase.feh.HeroUnit
import com.bloombase.feh.WinLoss

abstract class DonorHeal : Assist(Type.DONOR_HEAL) {
    override fun isValidPreCombat(
        self: HeroUnit,
        selfAttacks: List<CombatResult>?,
        possibleAttacks: Map<HeroUnit, List<CombatResult>>
    ): Boolean {
        return self.isEmptyHanded
    }

    override fun isValidPreCombatTarget(
        self: HeroUnit,
        target: HeroUnit
    ): Boolean {
        return target.currentHp < target.stat.hp
    }
}