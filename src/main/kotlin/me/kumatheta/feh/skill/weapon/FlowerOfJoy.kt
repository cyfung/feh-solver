package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.MagicB
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.*

private val BUFF = BasicSkill(inCombatStat = combatStartSkill(Stat(atk = 3, spd = 3)))

val FlowerOfJoy = BasicWeapon(MagicB, BasicSkill(
    extraStat = weaponStat(14, res = 3),
    supportInCombatBuff = {
        if (it.self.inCardinalDirection(it.targetAlly)) {
            BUFF
        } else {
            null
        }
    }
))
