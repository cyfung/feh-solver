package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.DaggerC
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.*

private const val ID = "Lyfjaberg"
val Lyfjaberg = BasicWeapon(DaggerC,
    BasicSkill(extraStat = weaponStat(14, res = 3),
        inCombatStat = { combatStatus ->
            if (combatStatus.self.hpThreshold(50) >= 0) {
                combatStatus.self.combatSkillData[ID] = true
                Stat(atk = 4, spd = 4)
            } else {
                Stat.ZERO
            }
        }, foeEffect = { combatStatus ->
            if (combatStatus.initAttack && combatStatus.self.hpThreshold(50) >= 0) {
                combatStartSkill(-1).toFollowUpPassive()
            } else {
                null
            }
        }, combatEnd = { combatStatus, attacked ->
            if (attacked) {
                aoeDebuffFoe(combatStatus, Stat(def = -7, res = -7))
                if (combatStatus.self.heroUnit.combatSkillData[ID] == true) {
                    combatStatus.self.heroUnit.cachedEffect.takeNonLethalDamage(4)
                }
            }
        }
    )
)