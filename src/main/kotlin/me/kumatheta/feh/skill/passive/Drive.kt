package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.*
import me.kumatheta.feh.skill.toInCombatStatPassive

class Drive(buff: Stat) : Passive {
    private val skill = combatStartSkill(buff).toInCombatStatPassive()

    override val supportInCombatBuff: SupportCombatEffect? = {
        if (it.targetAlly.position.distanceTo(it.self.position) <= 2) {
            skill
        } else {
            null
        }
    }

}

object GoadCavalry : Passive {
    private val EFFECT = object : Skill {
        override val inCombatStat: CombatStartSkill<Stat>? = combatSkill(Stat(atk = 4, spd = 4))
    }

    override val supportInCombatBuff: SupportCombatEffect? = {
        if (it.targetAlly.moveType == MoveType.CAVALRY && it.targetAlly.position.distanceTo(it.self.position) <= 2) {
            EFFECT
        } else {
            null
        }
    }

}