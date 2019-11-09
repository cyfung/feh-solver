package me.kumatheta.feh.skill.assist

import me.kumatheta.feh.CombatResult
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.NormalAssist
import kotlin.math.min

object Sacrifice : me.kumatheta.feh.NormalAssist() {
    override fun apply(self: HeroUnit, target: HeroUnit) {
        val heal = min(target.stat.hp - target.currentHp, self.currentHp - 1)
        if (heal > 0) {
            target.heal(heal)
            self.takeNonLethalDamage(heal)
        }
        target.applyBuff(-target.debuff)
        target.clearPenalty()
    }

    override fun isValidAction(self: HeroUnit, target: HeroUnit): Boolean {
        return target.debuff.isNotZero() || (target.currentHp < target.stat.hp && self.currentHp > 1)
    }

    override fun isValidPreCombat(
        self: HeroUnit,
        selfAttacks: List<CombatResult>
    ): Boolean {
        return selfAttacks.all {
            it.potentialDamage < 5
        }
    }

    override fun preCombatBestTarget(
        self: HeroUnit,
        targets: Set<HeroUnit>,
        lazyAllyThreat: Lazy<Set<HeroUnit>>,
        distanceToClosestEnemy: Map<HeroUnit, Int>
    ): HeroUnit? {
        return targets.asSequence().map { target ->
            target to min(target.stat.hp - target.currentHp, self.currentHp - 1)
        }.filter {
            it.second > 0
        }.bestHealTarget()
    }
}