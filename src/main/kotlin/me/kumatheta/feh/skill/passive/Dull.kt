package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.combatStartSkill

private val ZERO = BasicSkill(neutralizeBonus = combatStartSkill(Stat.ZERO))

val DullClose3 = BasicSkill(foeEffect = {
    if (it.foe.weaponType.isRanged) {
        null
    } else {
        ZERO
    }
})

val DullRange3 = BasicSkill(foeEffect = {
    if (it.foe.weaponType.isRanged) {
        ZERO
    } else {
        null
    }
})