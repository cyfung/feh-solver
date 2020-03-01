package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.skill.BasicSkill

fun pass(percentageHp: Int) = BasicSkill(pass = { _, self ->
    self.hpThreshold(percentageHp) >= 0
})