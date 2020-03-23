package me.kumatheta.feh.skill.effect

enum class BooleanAdjustment(val value: Int) {
    POSITIVE(1),
    NEUTRAL(0),
    NEGATIVE(-1)
}

interface SkillEffect

fun skillEffects(vararg skillEffects: SkillEffect): Sequence<SkillEffect> {
    return skillEffects.asSequence()
}