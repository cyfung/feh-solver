package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.skill.BasicSkill

fun obstruct(percentageHp: Int) = BasicSkill(obstruct = { _, self ->
    self.hpThreshold(percentageHp) >= 0
})