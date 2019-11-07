package com.bloombase.feh.skill.assist

import com.bloombase.feh.CombatResult
import com.bloombase.feh.HeroUnit
import com.bloombase.feh.NormalAssist

private const val HEAL_AMOUNT = 10

object ArdentSacrifice : NormalAssist() {
    override fun isValidAction(self: HeroUnit, target: HeroUnit): Boolean {
        return self.currentHp > HEAL_AMOUNT && target.currentHp < target.stat.hp
    }

    override fun apply(self: HeroUnit, target: HeroUnit) {
        self.takeNonLethalDamage(HEAL_AMOUNT)
        target.heal(HEAL_AMOUNT)
    }

    override fun isValidPreCombat(
        self: HeroUnit,
        selfAttacks: List<CombatResult>
    ): Boolean {
        return self.isEmptyHanded && self.currentHp > HEAL_AMOUNT
    }

    override fun preCombatBestTarget(
        self: HeroUnit,
        targets: Set<HeroUnit>,
        lazyAllyThreat: Lazy<Set<HeroUnit>>,
        distanceToClosestEnemy: Map<HeroUnit, Int>
    ): HeroUnit? {
        return targets.asSequence().filter { target ->
            target.stat.hp - target.currentHp >= HEAL_AMOUNT
        }.minWith(BASE_ASSIST_COMPARATOR)
    }

}