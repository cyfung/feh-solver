package me.kumatheta.feh.skill.assist

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.CombatResult
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Position

private const val HEAL_AMOUNT = 10

object ArdentSacrifice : me.kumatheta.feh.NormalAssist() {
    override fun isValidAction(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState,
        fromPosition: Position
    ): Boolean {
        return self.currentHp > HEAL_AMOUNT && target.currentHp < target.stat.hp
    }

    override fun apply(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState
    ) {
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