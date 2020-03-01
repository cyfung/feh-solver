package me.kumatheta.feh.skill.effect.supportincombat

import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.Skill
import me.kumatheta.feh.skill.combatStartSkill

fun spur(buff: Skill) = BasicSkill(
    supportInCombatBuff = {
        if (it.targetAlly.position.distanceTo(it.self.position) == 1) {
            buff
        } else {
            null
        }
    }
)

fun spur(stat: Stat) = spur(
    BasicSkill(
        inCombatStat = combatStartSkill(stat)
    )
)
