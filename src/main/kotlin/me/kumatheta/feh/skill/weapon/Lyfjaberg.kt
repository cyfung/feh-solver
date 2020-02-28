package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.BasicWeapon
import me.kumatheta.feh.CombatEndSkill
import me.kumatheta.feh.CombatStartSkill
import me.kumatheta.feh.DaggerC
import me.kumatheta.feh.InCombatSkill
import me.kumatheta.feh.Skill
import me.kumatheta.feh.Stat
import me.kumatheta.feh.combatSkill
import me.kumatheta.feh.skill.aoeDebuffFoe

object Lyfjaberg : BasicWeapon(DaggerC, 14, Stat(res = 3)) {
    override val inCombatStat: CombatStartSkill<Stat>? = { combatStatus ->
        if (combatStatus.self.hpThreshold(50) >= 0) {
            combatStatus.self.combatSkillData[this@Lyfjaberg] = true
            Stat(atk = 4, spd = 4)
        } else {
            Stat.ZERO
        }
    }

    override val foeEffect: CombatStartSkill<Skill?>? = { combatStatus ->
        if (combatStatus.initAttack && combatStatus.self.hpThreshold(50) >= 0) {
            object : Skill {
                override val followUp: InCombatSkill<Int>? = combatSkill(-1)
            }
        } else {
            null
        }
    }


    override val combatEnd: CombatEndSkill? = { combatStatus, attacked ->
        if (attacked) {
            aoeDebuffFoe(combatStatus, Stat(def = -7, res = -7))
            if (combatStatus.self.heroUnit.combatSkillData[this@Lyfjaberg] == true) {
                combatStatus.self.heroUnit.cachedEffect.takeNonLethalDamage(4)
            }
        }
    }

}