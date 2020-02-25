package me.kumatheta.feh.skill.special

import me.kumatheta.feh.skill.toSkillMap

val ALL_SPECIALS = sequenceOf(
    "Ice Mirror" to IceMirror,

    "Luna" to FoeDefResBased(3, 1, 2),
    "Moonbow" to FoeDefResBased(2, 3, 10),

    "Imbue" to Imbue,
    "Miracle" to Miracle,

    "Bonfire" to SelfStatBased(3) {
        it.def / 2
    },
    "Iceberg" to SelfStatBased(3) {
        it.res / 2
    },
    "Draconic Aura" to SelfStatBased(3) {
        it.atk * 3 / 10
    },
    "Dragon Fang" to SelfStatBased(4) {
        it.atk / 2
    }
).toSkillMap()