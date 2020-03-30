package me.kumatheta.feh.skill

import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.special.*

val ALL_SPECIALS = sequenceOf(
    "Ice Mirror" to IceMirror,
    "Sirius" to Sirius,
    "Galeforce" to Galeforce,

    "Blazing Flame" to BlazingFlame,
    "Blazing Light" to BlazingLight,
    "Blazing Wind" to BlazingWind,
    "Blazing Thunder" to BlazingThunder,
    "Growing Thunder" to GrowingThunder,

    "Aether" to Aether(5),
    "Radiant Aether" to Aether(4),
    "Moonbow" to FoeDefResBased(2, 3, 10),
    "Luna" to FoeDefResBased(3, 1, 2),
    "Sacred Cowl" to SacredCowl,
    "Escutcheon" to Escutcheon,

    "Windfire Balm+" to Balm(Stat(atk=6,spd=6)),

    "Imbue" to Imbue,
    "Heavenly Light" to HeavenlyLight,
    "Miracle" to Miracle,

    "Reprisal" to HpMissingBased(2,3, 10),
    "Vengeance" to HpMissingBased(2,1, 2),
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
        it.res * 4 / 5
    },
    "Draconic Aura" to SelfStatBased(3) {
        it.atk * 3 / 10
    },
    "Dragon Fang" to SelfStatBased(4) {
        it.atk / 2
    },
    "Glimmer" to DamageAmplify(2, 50),
    "Astra" to DamageAmplify(2, 150)
).toSkillMap()