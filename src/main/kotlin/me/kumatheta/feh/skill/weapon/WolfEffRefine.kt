package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.*

class EffectiveAndNeutralize(weaponType: WeaponType, might: Int, moveType: MoveType, extraStat: Stat = Stat.ZERO) :
    BasicWeapon(weaponType, might, extraStat) {
    override val effectiveAgainstMoveType: Set<MoveType> = setOf(moveType)

    override val neutralizeBonus: CombatStartSkill<Stat?>? = {
        if (it.foe.moveType == moveType) {
            Stat.ZERO
        } else {
            null
        }
    }
}

fun WeaponType.effectiveAndNeutralize(
    might: Int,
    moveType: MoveType,
    extraStat: Stat = Stat.ZERO
): EffectiveAndNeutralize {
    return EffectiveAndNeutralize(this, might, moveType, extraStat)
}