package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.skill.BasicSkill

val DragonAdaptive = BasicSkill(
    adaptiveDamage = {
        it.foe.weaponType.isRanged
    }
)