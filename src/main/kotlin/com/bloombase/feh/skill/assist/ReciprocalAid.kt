package com.bloombase.feh.skill.assist

import com.bloombase.feh.CombatResult
import com.bloombase.feh.HeroUnit
import com.bloombase.feh.NormalAssist

object ReciprocalAid : NormalAssist() {
    override fun apply(self: HeroUnit, target: HeroUnit) {
        val healAmount = target.currentHp - self.currentHp
        if (healAmount > 0) {
            target.heal(healAmount)
            self.takeNonLethalDamage(healAmount)
        } else {
            target.takeNonLethalDamage(-healAmount)
            self.heal(-healAmount)
        }
    }

    override fun isValidAction(self: HeroUnit, target: HeroUnit): Boolean {
        return (target.currentHp < target.stat.hp && self.currentHp > target.currentHp) ||
                (self.currentHp < self.stat.hp && target.currentHp > self.currentHp)
    }

    override fun isValidPreCombat(
        self: HeroUnit,
        selfAttacks: List<CombatResult>
    ): Boolean {
        return self.isEmptyHanded
    }

    override fun preCombatBestTarget(
        self: HeroUnit,
        targets: Set<HeroUnit>,
        lazyAllyThreat: Lazy<Set<HeroUnit>>,
        distanceToClosestEnemy: Map<HeroUnit, Int>
    ): HeroUnit? {
        return targets.asSequence().filter { target ->
            target.stat.hp > target.currentHp &&
                    self.currentHp > target.currentHp &&
                    target.stat.hp >= self.currentHp &&
                    self.stat.hp >= target.currentHp
        }.map { target ->
            target to (self.currentHp - target.currentHp)
        }.bestHealTarget()
    }
}
