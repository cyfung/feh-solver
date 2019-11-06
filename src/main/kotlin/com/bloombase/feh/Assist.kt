package com.bloombase.feh

sealed class Assist : Skill
abstract class MovementAssist : Assist()

abstract class NormalAssist : Assist() {
    abstract fun isValidPreCombat(
        self: HeroUnit,
        selfAttacks: List<CombatResult>
    ): Boolean

    abstract fun preCombatBestTarget(
        self: HeroUnit,
        targets: Set<HeroUnit>,
        lazyAllyThreat: Lazy<Set<HeroUnit>>,
        distanceToClosestEnemy: Map<HeroUnit, Int>
    ): HeroUnit?
}

class AssistEffect(val type: Type) {
    enum class Type {
        REFRESH,
        HEAL,
        DONOR_HEAL,
        RALLY
    }

}