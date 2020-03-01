package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.MagicR
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.BasicWeapon
import me.kumatheta.feh.skill.weaponStat

private val IN_COMBAT_BUF = Stat(atk = 5, spd = 5)

private const val ID = "Ragnarok"

val Ragnarok = BasicWeapon(MagicR, BasicSkill(
    extraStat = weaponStat(14),
    inCombatStat = { combatStatus ->
        if (combatStatus.self.currentHp == combatStatus.self.maxHp) {
            combatStatus.self.combatSkillData[ID] = true
            IN_COMBAT_BUF
        } else {
            Stat.ZERO
        }
    }, combatEnd = { combatStatus, attacked ->
        if (attacked) {
            if (combatStatus.self.heroUnit.combatSkillData[ID] == true) {
                combatStatus.self.heroUnit.cachedEffect.takeNonLethalDamage(5)
            }
        }
    }
))