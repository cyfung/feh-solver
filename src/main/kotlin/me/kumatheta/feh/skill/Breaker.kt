package me.kumatheta.feh.skill

import me.kumatheta.feh.InCombatSkill
import me.kumatheta.feh.WeaponType

fun breaker(weaponType: WeaponType, percentage: Int): InCombatSkill<Int> {
    return {
        if (it.foe.heroUnit.weaponType == weaponType && it.self.heroUnit.hpThreshold(percentage) >= 0) {
            1
        } else {
            0
        }
    }
}