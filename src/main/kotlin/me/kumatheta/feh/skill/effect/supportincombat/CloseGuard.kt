package me.kumatheta.feh.skill.effect.supportincombat

import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.Passive
import me.kumatheta.feh.skill.combatStartSkill

fun closeGuard(buff: Passive) = BasicSkill(
    supportInCombatBuff = {
        if (!it.targetFoe.weaponType.isRanged && it.targetAlly.position.distanceTo(it.self.position) <= 2) {
            buff
        } else {
            null
        }
    }
)

fun closeGuard(buff: Stat) =
    closeGuard(
        BasicSkill(
            inCombatStat = combatStartSkill(
                buff
            )
        )
    )