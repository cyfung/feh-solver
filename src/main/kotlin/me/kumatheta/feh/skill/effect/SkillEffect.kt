package me.kumatheta.feh.skill.effect

enum class BooleanAdjustment(val value: Int) {
    POSITIVE(1),
    NEUTRAL(0),
    NEGATIVE(-1)
}

interface SkillEffect

fun <T: SkillEffect> skillEffects(vararg skillEffects: T): Sequence<T> {
    return skillEffects.asSequence()
}

interface InCombatSkillEffect : SkillEffect