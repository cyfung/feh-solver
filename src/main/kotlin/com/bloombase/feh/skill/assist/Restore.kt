package com.bloombase.feh.skill.assist

import com.bloombase.feh.*

abstract class Restore : NormalAssist() {
    override fun isValidPreCombat(
        self: HeroUnit,
        selfAttacks: List<CombatResult>?,
        possibleAttacks: Map<HeroUnit, List<CombatResult>>
    ): Boolean {
        return !possibleAttacks.asSequence().filterNot { it.key == self }.none()
    }
}