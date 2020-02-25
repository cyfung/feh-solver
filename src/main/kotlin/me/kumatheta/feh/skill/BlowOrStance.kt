package me.kumatheta.feh.skill

import me.kumatheta.feh.CombatStartSkill
import me.kumatheta.feh.Stat

fun blowOrStance(blow: Stat, stance: Stat): CombatStartSkill<Stat> {
    return {
        if (it.initAttack) {
            blow
        } else {
            stance
        }
    }
}