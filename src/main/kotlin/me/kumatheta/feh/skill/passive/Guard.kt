package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.CooldownChange
import me.kumatheta.feh.skill.InCombatStat
import me.kumatheta.feh.skill.effect.CoolDownChargeEffect
import me.kumatheta.feh.skill.effect.EffectOnFoe
import me.kumatheta.feh.skill.effect.SkillEffect

private val COOL_DOWN_DEBUFF = CooldownChange(-1, -1)

private val FOE_EFFECT = object: CoolDownChargeEffect {
    override fun getAdjustment(combatStatus: CombatStatus<InCombatStat>): CooldownChange {
        return COOL_DOWN_DEBUFF
    }
}

class Guard(private val percentageHp: Int) : EffectOnFoe {
    override fun apply(combatStatus: CombatStatus<HeroUnit>): Sequence<SkillEffect> {
        return if (combatStatus.self.hpThreshold(80) >= 0) {
            sequenceOf(FOE_EFFECT)
        } else {
            emptySequence()
        }
    }
}


