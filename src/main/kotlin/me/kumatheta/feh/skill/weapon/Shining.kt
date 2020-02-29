package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.*

class Shining(weaponType: WeaponType, might: Int, extraStat: Stat = Stat.ZERO) :
    BasicWeapon(weaponType, might, extraStat) {
    override val damageIncrease: (CombatStatus<InCombatStat>, specialTriggered: Boolean) -> Int =
        { combatStatus, specialTriggered ->
            if (combatStatus.foe.inCombatStat.def >= combatStatus.foe.inCombatStat.res + 5) {
                7
            } else {
                0
            }
        }
}

fun WeaponType.shining(might: Int, extraStat: Stat = Stat.ZERO): Shining {
    return Shining(this, might, extraStat)
}