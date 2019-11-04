package com.bloombase.feh

sealed class Assist : Skill
abstract class MovementAssist : Assist()

abstract class NormalAssist : Assist() {
    abstract fun isValidPreCombat(
        self: HeroUnit,
        selfAttacks: List<CombatResult>?,
        possibleAttacks: Map<HeroUnit, List<CombatResult>>
    ): Boolean

    abstract fun preCombatAssistEffect(self: HeroUnit, target: HeroUnit): AssistEffect?
}

class AssistEffect(val type: Type) {
    enum class Type {
        REFRESH,
        HEAL,
        DONOR_HEAL,
        RALLY
    }

}