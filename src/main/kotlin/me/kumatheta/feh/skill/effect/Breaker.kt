package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.CombatStartSkill
import me.kumatheta.feh.InCombatSkill
import me.kumatheta.feh.WeaponType

fun breaker(weaponType: WeaponType, percentage: Int): CombatStartSkill<Int> {
    return {
        if (it.foe.weaponType == weaponType && it.self.hpThreshold(percentage) >= 0) {
            1
        } else {
            0
        }
    }
}