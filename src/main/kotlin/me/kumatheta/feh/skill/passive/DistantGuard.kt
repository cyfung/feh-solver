package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.*

fun distantGuard(buff: Passive): SupportCombatEffect = {
    if (it.targetFoe.weaponType.isRanged && it.targetAlly.position.distanceTo(it.self.position) <= 2) {
        buff
    } else {
        null
    }
}

fun distantGuard(buff: Stat) = distantGuard(combatStartSkill(buff).toInCombatStatPassive())