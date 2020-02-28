package me.kumatheta.feh.skill

import me.kumatheta.feh.skill.special.BlazingLight
import me.kumatheta.feh.skill.special.DamageAmplify
import me.kumatheta.feh.skill.special.FoeDefResBased
import me.kumatheta.feh.skill.special.HeavenlyLight
import me.kumatheta.feh.skill.special.IceMirror
import me.kumatheta.feh.skill.special.Imbue
import me.kumatheta.feh.skill.special.Miracle
import me.kumatheta.feh.skill.special.SelfStatBased

val ALL_SPECIALS = sequenceOf(
    "Ice Mirror" to IceMirror,

    "Blazing Light" to BlazingLight,

    "Luna" to FoeDefResBased(3, 1, 2),
    "Moonbow" to FoeDefResBased(2, 3, 10),

    "Imbue" to Imbue,
    "Heavenly Light" to HeavenlyLight,
    "Miracle" to Miracle,

    "Bonfire" to SelfStatBased(3) {
        it.def / 2
    },
    "Iceberg" to SelfStatBased(3) {
        it.res / 2
    },
    "Ignis" to SelfStatBased(4) {
        it.def * 4 / 5
    },
    "Glacies" to SelfStatBased(4) {
        it.def * 4 / 5
    },
    "Draconic Aura" to SelfStatBased(3) {
        it.atk * 3 / 10
    },
    "Dragon Fang" to SelfStatBased(4) {
        it.atk / 2
    },
    "Glimmer" to DamageAmplify(2, 50)
).toSkillMap()