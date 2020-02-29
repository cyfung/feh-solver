package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.*
import me.kumatheta.feh.skill.toFollowUpPassive

private val FOE_EFFECT = combatStartSkill(-1).toFollowUpPassive()

class WaryFighter(percentageHp: Int) : Passive {
    override val followUp: CombatStartSkill<Int>? = {
        if (it.self.hpThreshold(percentageHp) >= 0) {
            -1
        } else {
            0
        }
    }
    override val foeEffect: CombatStartSkill<Skill?>? = {
        if (it.self.hpThreshold(percentageHp) >= 0) {
            FOE_EFFECT
        } else {
            null
        }
    }
}