package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.SupportCombatEffect
import me.kumatheta.feh.skill.combatStartSkill
import me.kumatheta.feh.skill.toInCombatStatPassive

fun spur(stat: Stat): SupportCombatEffect = {
    if (it.targetAlly.position.distanceTo(it.self.position) == 1) {
        combatStartSkill(stat).toInCombatStatPassive()
    } else {
        null
    }
}
