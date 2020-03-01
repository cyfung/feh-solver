package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.CombatStartSkill
import me.kumatheta.feh.skill.Skill
import me.kumatheta.feh.skill.combatStartSkill


fun lull(stat: Stat): BasicSkill {
    val foeEffect = BasicSkill(
        neutralizeBonus =
        combatStartSkill(
            Stat(
                atk = if (stat.atk > 0) 0 else 1,
                spd = if (stat.spd > 0) 0 else 1,
                def = if (stat.def > 0) 0 else 1,
                res = if (stat.res > 0) 0 else 1
            )
        ), inCombatStat = combatStartSkill(stat)
    )
    return BasicSkill(foeEffect = {
        foeEffect
    })
}