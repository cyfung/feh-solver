package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.Stat
import me.kumatheta.feh.StatType
import me.kumatheta.feh.skill.effect.EffectOnFoe
import me.kumatheta.feh.skill.effect.EffectOnFoeBasic
import me.kumatheta.feh.skill.effect.NeutralizeBonusBasic
import me.kumatheta.feh.skill.effect.skillEffects
import me.kumatheta.feh.skill.toInCombatStatEffect

fun lull(stat: Stat): EffectOnFoe {
    val statTypes = sequence {
        if (stat.atk > 0) yield(StatType.ATK)
        if (stat.spd > 0) yield(StatType.SPD)
        if (stat.def > 0) yield(StatType.DEF)
        if (stat.res > 0) yield(StatType.RES)
    }.toSet()
    val foeEffect = skillEffects(
        NeutralizeBonusBasic(statTypes),
        stat.toInCombatStatEffect()
    ).toList()
    return EffectOnFoeBasic(foeEffect)
}