package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.CombatStartSkill
import me.kumatheta.feh.Passive
import me.kumatheta.feh.Skill
import me.kumatheta.feh.Stat
import me.kumatheta.feh.combatStartSkill


class Lull(stat: Stat) : Passive {
    private val _foeEffect = object : Skill {
        override val neutralizeBonus: CombatStartSkill<Stat?>? = combatStartSkill(
            Stat(
                atk = if (stat.atk > 0) 0 else 1,
                spd = if (stat.spd > 0) 0 else 1,
                def = if (stat.def > 0) 0 else 1,
                res = if (stat.res > 0) 0 else 1
            )
        )
        override val inCombatStat = combatStartSkill(stat)
    }
    override val foeEffect: CombatStartSkill<Skill?>? = {
        _foeEffect
    }


}