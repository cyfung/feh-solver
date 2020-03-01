package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.MoveType
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.*

fun drive(buff: Stat, moveType: MoveType? = null) = drive(combatStartSkill(buff).toInCombatStatPassive(), moveType)

fun drive(buff: Skill, moveType: MoveType?): SupportCombatEffect {
    if(moveType == null) {
        return {
            if (it.targetAlly.position.distanceTo(it.self.position) <= 2) {
                buff
            } else {
                null
            }
        }
    } else {
        return {
            if (it.targetAlly.moveType == moveType && it.targetAlly.position.distanceTo(it.self.position) <= 2) {
                buff
            } else {
                null
            }
        }
    }
}