package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.*
import me.kumatheta.feh.skill.inCardinalDirection
import me.kumatheta.feh.skill.toInCombatStatPassive

private val IN_COMBAT_BUFF = Stat(atk = 3, spd = 3)

object FlowerOfJoy : BasicWeapon(MagicB, 14, Stat(res = 3)) {
    override val supportInCombatBuff: SupportCombatEffect? = {
        if (it.self.inCardinalDirection(it.targetAlly)) {
                combatStartSkill(IN_COMBAT_BUFF).toInCombatStatPassive()
            } else {
                null
            }
        }
    }
