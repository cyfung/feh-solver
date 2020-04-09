package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.DragonC
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.InCombatStat
import me.kumatheta.feh.skill.basic
import me.kumatheta.feh.skill.effect.PostCombatEffect
import me.kumatheta.feh.skill.effect.others.DragonAdaptive
import me.kumatheta.feh.skill.nearbyAllies
import me.kumatheta.feh.skill.nearbyAlliesAndSelf
import me.kumatheta.feh.skill.plus

private val DEBUFF = Stat(atk = -5, spd = -5)
private val REFINE_DEBUFF = Stat(atk = -7, spd = -7)

val DarkBreathPlus = DragonC.basic(13) + object : PostCombatEffect {
    override fun onCombatEnd(combatStatus: CombatStatus<InCombatStat>, attacked: Boolean) {
        if (combatStatus.initAttack) {
            combatStatus.foe.heroUnit.nearbyAllies(combatStatus.battleState, 2).forEach {
                it.cachedEffect.applyDebuff(DEBUFF)
            }
        }
    }
}

val DarkBreathPlusRefine = DragonC.basic(14) + sequenceOf(
    DragonAdaptive,
    object : PostCombatEffect {
        override fun onCombatEnd(combatStatus: CombatStatus<InCombatStat>, attacked: Boolean) {
            if (attacked) {
                combatStatus.foe.heroUnit.nearbyAlliesAndSelf(combatStatus.battleState, 2).forEach {
                    it.cachedEffect.applyDebuff(REFINE_DEBUFF)
                }
            }
        }
    }
)