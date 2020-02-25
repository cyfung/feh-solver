package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.CombatStartSkill
import me.kumatheta.feh.Passive
import me.kumatheta.feh.Skill
import me.kumatheta.feh.Stat
import me.kumatheta.feh.combatSkill


object DullClose3 : Passive {
    override val foeEffect: CombatStartSkill<Skill?>? = {
        if (!it.foe.weaponType.isRanged) {
            null
        } else {
            object : Skill {
                override val neutralizeBonus: CombatStartSkill<Stat?>? = combatSkill(Stat.ZERO)
            }
        }
    }
}