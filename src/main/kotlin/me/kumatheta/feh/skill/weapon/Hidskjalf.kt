package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.Staff
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.InCombatStat
import me.kumatheta.feh.skill.basic
import me.kumatheta.feh.skill.effect.DisableCounter
import me.kumatheta.feh.skill.effect.PostCombatEffect
import me.kumatheta.feh.skill.effect.skillEffects
import me.kumatheta.feh.skill.nearbyAllies
import me.kumatheta.feh.skill.plus

private val BUFF = Stat(atk = 4, spd = 4, def = 4, res = 4)
private val DEBUFF = Stat(atk = -4, spd = -4, def = -4, res = -4)

val Hidskjalf = Staff.basic(14) + skillEffects(
    DisableCounter,
    object : PostCombatEffect {
        override fun onCombatEnd(combatStatus: CombatStatus<InCombatStat>, attacked: Boolean) {
            if (attacked) {
                val foe = combatStatus.foe.heroUnit
                (foe.nearbyAllies(combatStatus.battleState, 2) + foe).forEach {
                    it.applyDebuff(DEBUFF)
                }
                val self = combatStatus.self.heroUnit
                (self.nearbyAllies(combatStatus.battleState, 2) + self).forEach {
                    it.applyBuff(BUFF)
                }
            }
        }
    }
)