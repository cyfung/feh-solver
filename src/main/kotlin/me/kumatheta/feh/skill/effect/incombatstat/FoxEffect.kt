package me.kumatheta.feh.skill.effect.incombatstat

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.effect.EffectOnFoe
import me.kumatheta.feh.skill.effect.EffectOnFoeBasic
import me.kumatheta.feh.skill.effect.InCombatSkillEffect
import me.kumatheta.feh.skill.effect.InCombatStatEffect
import me.kumatheta.feh.skill.toInCombatStatEffect

class FoxEffect(private val stat: Stat): EffectOnFoe {
    private val foeEffect = stat.toInCombatStatEffect()
    override fun apply(combatStatus: CombatStatus<HeroUnit>): Sequence<InCombatSkillEffect> {
        return if (combatStatus.initAttack) {
            sequenceOf(foeEffect)
        } else {
            emptySequence()
        }
    }
}

fun fox(atk: Int = 0,
         spd: Int = 0,
         def: Int = 0,
         res: Int = 0
) = FoxEffect(Stat(atk = atk, spd = spd, def = def, res = res))