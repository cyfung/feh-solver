package me.kumatheta.feh.skill.effect.supportincombat

import me.kumatheta.feh.MoveType
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.Skill
import me.kumatheta.feh.skill.combatStartSkill

fun drive(buff: Stat, moveType: MoveType? = null) =
    drive(
        BasicSkill(
            inCombatStat = combatStartSkill(buff)
        ), moveType
    )

fun drive(buff: Skill, moveType: MoveType?) =
    if (moveType == null) {
        BasicSkill(
            supportInCombatBuff = {
                if (it.targetAlly.position.distanceTo(it.self.position) <= 2) {
                    buff
                } else {
                    null
                }
            }
        )
    } else {
        BasicSkill(
            supportInCombatBuff =
            {
                if (it.targetAlly.moveType == moveType && it.targetAlly.position.distanceTo(it.self.position) <= 2) {
                    buff
                } else {
                    null
                }
            }
        )
    }

