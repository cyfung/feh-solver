package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.CombatStartSkill
import me.kumatheta.feh.Passive
import me.kumatheta.feh.Skill
import me.kumatheta.feh.Stat
import me.kumatheta.feh.combatSkill
import me.kumatheta.feh.combatStartSkill

private val ZERO = combatStartSkill(Stat.ZERO).toInCombatStatPassive()

object DullClose3 : Passive {
    override val foeEffect: CombatStartSkill<Skill?>? = {
        if (!it.foe.weaponType.isRanged) {
            null
        } else {
            ZERO
        }
    }
}

object DullRange3 : Passive {
    override val foeEffect: CombatStartSkill<Skill?>? = {
        if (!it.foe.weaponType.isRanged) {
            ZERO
        } else {
            null
        }
    }
}