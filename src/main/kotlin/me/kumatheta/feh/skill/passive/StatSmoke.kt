package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.NegativeStatus
import me.kumatheta.feh.Stat
import me.kumatheta.feh.foe
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.InCombatStat
import me.kumatheta.feh.skill.effect.PostCombatEffect
import me.kumatheta.feh.skill.effect.SpecialDebuff
import me.kumatheta.feh.skill.effect.skillEffects
import me.kumatheta.feh.skill.nearbyAlliesAndSelf
import me.kumatheta.feh.skill.toSkill

class SpecialSmoke(private val effect: (HeroUnit) -> Unit) : PostCombatEffect {
    override fun onCombatEnd(combatStatus: CombatStatus<InCombatStat>, attacked: Boolean) {
        if (!combatStatus.self.heroUnit.isDead) {
            combatStatus.foe.heroUnit.nearbyAlliesAndSelf(combatStatus.battleState, 2).forEach {
                effect(it)
            }
        }
    }
}

class StatSmoke(private val effect: (HeroUnit) -> Unit) : PostCombatEffect {
    override fun onCombatEnd(combatStatus: CombatStatus<InCombatStat>, attacked: Boolean) {
        if (attacked && !combatStatus.self.heroUnit.isDead) {
            combatStatus.foe.heroUnit.nearbyAlliesAndSelf(combatStatus.battleState, 2).forEach {
                effect(it)
            }
        }
    }
}

fun statSmoke(debuff: Stat): StatSmoke {
    return StatSmoke {
        it.cachedEffect.applyDebuff(debuff)
    }
}

fun statSmoke(
    atk: Int = 0,
    spd: Int = 0,
    def: Int = 0,
    res: Int = 0
) = statSmoke(Stat(atk = atk, spd = spd, def = def, res = res))


val PulseSmoke3 = SpecialSmoke {
    it.cachedEffect.cooldown++
}

val PanicSmoke3 = skillEffects(SpecialSmoke {
    it.addNegativeStatus(NegativeStatus.PANIC)
}, SpecialDebuff.ONLY_WHEN_ALIVE).toSkill()
