package me.kumatheta.feh.skill.effect.supportincombat

import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.Passive
import me.kumatheta.feh.skill.combatStartSkill

fun distantGuard(buff: Passive) = BasicSkill(
    supportInCombatBuff = {
        if (it.targetFoe.weaponType.isRanged && it.targetAlly.position.distanceTo(it.self.position) <= 2) {
            buff
        } else {
            null
        }
    }
)

fun distantGuard(buff: Stat) =
    distantGuard(
        BasicSkill(
            inCombatStat = combatStartSkill(
                buff
            )
        )
    )