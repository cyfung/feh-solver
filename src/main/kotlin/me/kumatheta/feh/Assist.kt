package me.kumatheta.feh

sealed class Assist : me.kumatheta.feh.Skill {
    abstract fun apply(self: me.kumatheta.feh.HeroUnit, target: me.kumatheta.feh.HeroUnit)
    abstract fun isValidAction(self: me.kumatheta.feh.HeroUnit, target: me.kumatheta.feh.HeroUnit): Boolean
}
abstract class MovementAssist : me.kumatheta.feh.Assist()

abstract class NormalAssist : me.kumatheta.feh.Assist() {
    abstract fun isValidPreCombat(
        self: me.kumatheta.feh.HeroUnit,
        selfAttacks: List<me.kumatheta.feh.CombatResult>
    ): Boolean

    abstract fun preCombatBestTarget(
        self: me.kumatheta.feh.HeroUnit,
        targets: Set<me.kumatheta.feh.HeroUnit>,
        lazyAllyThreat: Lazy<Set<me.kumatheta.feh.HeroUnit>>,
        distanceToClosestEnemy: Map<me.kumatheta.feh.HeroUnit, Int>
    ): me.kumatheta.feh.HeroUnit?
}

class AssistEffect(val type: me.kumatheta.feh.AssistEffect.Type) {
    enum class Type {
        REFRESH,
        HEAL,
        DONOR_HEAL,
        RALLY
    }

}