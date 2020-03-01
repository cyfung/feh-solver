package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.Skill
import me.kumatheta.feh.skill.SupportCombatEffect
import me.kumatheta.feh.skill.combatStartSkill
import me.kumatheta.feh.skill.toInCombatStatPassive

fun spur(buff: Skill): SupportCombatEffect = {
    if (it.targetAlly.position.distanceTo(it.self.position) == 1) {
        buff
    } else {
        null
    }
}

fun spur(stat: Stat) = spur(combatStartSkill(stat).toInCombatStatPassive())
