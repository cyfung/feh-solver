package me.kumatheta.feh.skill.effect

interface CancelAffinity : CombatStartEffect<CancelAffinity.Type?>, SkillEffect {
    enum class Type {
        CANCEL_AFFINITY_1,
        CANCEL_AFFINITY_2,
        CANCEL_AFFINITY_3
    }
}