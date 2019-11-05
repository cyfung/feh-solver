package com.bloombase.feh.skill.assist

import com.bloombase.feh.CombatResult
import com.bloombase.feh.HeroUnit
import com.bloombase.feh.NormalAssist
import com.bloombase.feh.min
import kotlin.math.min

private const val HEAL_AMOUNT = 10

object Sacrifice : NormalAssist() {
    override fun isValidPreCombat(
        self: HeroUnit,
        selfAttacks: List<CombatResult>
    ): Boolean {
        return selfAttacks.all {
            it.potentialDamage < 5
        }
    }

    override fun preCombatBestTarget(self: HeroUnit, targets: Set<HeroUnit>): HeroUnit? {
        return targets.asSequence().map { target ->
            target to min(target.stat.hp - target.currentHp, self.currentHp - 1)
        }.filter {
            it.second > 0
        }.bestHealTarget()
    }
}