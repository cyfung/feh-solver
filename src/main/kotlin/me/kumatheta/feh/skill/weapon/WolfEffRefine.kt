package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.*

class WolfEffRefine(weaponType: WeaponType, might: Int, extraStat: Stat = Stat.ZERO) :
    BasicWeapon(weaponType, might, extraStat) {
    override val effectiveAgainstMoveType: Set<MoveType> = setOf(MoveType.CAVALRY)

    override val neutralizeBonus: CombatStartSkill<Stat?>? = {
        if (it.foe.moveType == MoveType.CAVALRY) {
            Stat.ZERO
        } else {
            null
        }
    }
}

