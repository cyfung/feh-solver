package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.Passive
import me.kumatheta.feh.Stat
import me.kumatheta.feh.SupportCombatEffect
import me.kumatheta.feh.combatStartSkill
import me.kumatheta.feh.skill.toInCombatStatPassive

class DistantGuard(buff: Stat) : Passive {
    private val skill = combatStartSkill(buff).toInCombatStatPassive()

    override val supportInCombatBuff: SupportCombatEffect = {
        if (it.targetFoe.weaponType.isRanged && it.targetAlly.position.distanceTo(it.self.position) <= 2) {
            skill
        } else {
            null
        }
    }

}