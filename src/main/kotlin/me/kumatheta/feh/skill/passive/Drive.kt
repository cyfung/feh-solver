package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.MoveType
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.*

class Drive(buff: Stat) : BasicSkill() {
    private val skill = combatStartSkill(buff).toInCombatStatPassive()

    override val supportInCombatBuff: SupportCombatEffect? = {
        if (it.targetAlly.position.distanceTo(it.self.position) <= 2) {
            skill
        } else {
            null
        }
    }

}

object GoadCavalry : BasicSkill() {
    private val EFFECT = BasicSkill(
        inCombatStat = combatSkill(Stat(atk = 4, spd = 4))
    )

    override val supportInCombatBuff: SupportCombatEffect? = {
        if (it.targetAlly.moveType == MoveType.CAVALRY && it.targetAlly.position.distanceTo(it.self.position) <= 2) {
            EFFECT
        } else {
            null
        }
    }

}