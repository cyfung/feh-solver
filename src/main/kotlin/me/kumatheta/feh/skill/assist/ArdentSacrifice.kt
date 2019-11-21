package me.kumatheta.feh.skill.assist

import me.kumatheta.feh.*

private const val HEAL_AMOUNT = 10

object ArdentSacrifice : NormalAssist() {
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
        return self.isEmptyHanded
    }

    private fun bestTarget(targets: Set<HeroUnit>): HeroUnit? {
        return targets.asSequence().filter { target ->
            target.stat.hp - target.currentHp >= HEAL_AMOUNT
        }.minWith(BASE_ASSIST_COMPARATOR)
    }

    override fun preCombatBestTarget(
        self: HeroUnit,
        targets: Set<HeroUnit>,
        lazyAllyThreat: Lazy<Map<HeroUnit, Set<HeroUnit>>>,
        distanceToClosestFoe: Map<HeroUnit, Int>
    ): HeroUnit? {
        return bestTarget(targets)
    }

    override fun postCombatBestTarget(
        self: HeroUnit,
        targets: Set<HeroUnit>,
        lazyAllyThreat: Lazy<Map<HeroUnit, Set<HeroUnit>>>,
        foeThreat: Map<Position, Int>,
        distanceToClosestFoe: Map<HeroUnit, Int>,
        battleState: BattleState
    ): HeroUnit? {
        return bestTarget(targets)
    }

}