package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.Staff
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.*

private val BUFF = Stat(atk = 4, spd = 4, def = 4, res = 4)
private val DEBUFF = Stat(atk = -4, spd = -4, def = -4, res = -4)

val Hidskjalf = BasicWeapon(
    Staff, BasicSkill(
        extraStat = Stat(atk = 14),
        foeEffect = DISABLE_COUNTER_EFFECT,
        combatEnd = { combatStatus: CombatStatus<InCombatStat>, attacked: Boolean ->
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
    )
)