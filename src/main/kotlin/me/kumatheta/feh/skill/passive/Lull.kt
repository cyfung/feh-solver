package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.StatType
import me.kumatheta.feh.skill.effect.EffectOnFoe
import me.kumatheta.feh.skill.effect.EffectOnFoeBasic
import me.kumatheta.feh.skill.effect.InCombatSkillEffect
import me.kumatheta.feh.skill.effect.NeutralizeBonusBasic
import me.kumatheta.feh.skill.effect.incombatstat.bond
import me.kumatheta.feh.skill.effect.skillEffects
import me.kumatheta.feh.skill.toInCombatStatEffect
import me.kumatheta.feh.statPairSequence
import me.kumatheta.feh.toStat

fun lull(statTypes: Pair<StatType, StatType>, value: Int): EffectOnFoe {
    val foeEffect: List<InCombatSkillEffect> = skillEffects<InCombatSkillEffect>(
        NeutralizeBonusBasic(statTypes.toList()),
        statTypes.toStat(value).toInCombatStatEffect()
    ).toList()
    return EffectOnFoeBasic(foeEffect)
}

fun allLull() = statPairSequence {
    val baseName = "Lull ${it.first}/${it.second}"
    sequenceOf(
        "$baseName 1" to lull(it, 1),
        "$baseName 2" to lull(it, 2),
        "$baseName 3" to lull(it, 3)
    )
}.flatMap { it }