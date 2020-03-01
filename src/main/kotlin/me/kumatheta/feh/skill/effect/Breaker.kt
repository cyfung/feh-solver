package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.WeaponType
import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.combatStartSkill

private val FOE_EFFECT = BasicSkill(followUp = combatStartSkill(-1))

fun breaker(weaponType: WeaponType, percentage: Int) = BasicSkill(
    followUp = {
        if (it.foe.weaponType == weaponType && it.self.hpThreshold(percentage) >= 0) {
            1
        } else {
            0
        }
    },
    foeEffect = {
        if (it.foe.weaponType == weaponType && it.self.hpThreshold(percentage) >= 0) {
            FOE_EFFECT
        } else {
            null
        }
    }
)