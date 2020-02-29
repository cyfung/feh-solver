package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.PerAttackSkill

val specialDamage: PerAttackSkill<Int>? = { _, specialTriggered ->
    if (specialTriggered) {
        10
    } else {
        0
    }
}