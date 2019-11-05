package com.bloombase.feh.skill.assist

import com.bloombase.feh.CombatResult
import com.bloombase.feh.HeroUnit
import com.bloombase.feh.NormalAssist

private const val HEAL_AMOUNT = 10

object ArdentSacrifice : NormalAssist() {
    override fun isValidPreCombat(
        self: HeroUnit,
        selfAttacks: List<CombatResult>
    ): Boolean {
        return self.isEmptyHanded && self.currentHp > HEAL_AMOUNT
    }

    override fun preCombatBestTarget(self: HeroUnit, targets: Set<HeroUnit>): HeroUnit? {
        return targets.asSequence().filter { target ->
            target.stat.hp - target.currentHp >= HEAL_AMOUNT
        }.maxWith(compareBy(
            { it.currentStatTotal },
            { -it.id }
        ))
    }

}