package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.WeaponType
import me.kumatheta.feh.skill.BasicSkill

fun breaker(weaponType: WeaponType, percentage: Int) = BasicSkill(
    followUp = {
        if (it.foe.weaponType == weaponType && it.self.hpThreshold(percentage) >= 0) {
            1
        } else {
            0
        }
    }
)