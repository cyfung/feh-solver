package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.*

private val FOE_EFFECT = object : Skill {
    override val neutralizeBonus: CombatStartSkill<Stat?>? = combatSkill(Stat.ZERO)
}

class EffectiveAndNeutralize(weaponType: WeaponType, might: Int, moveType: MoveType, extraStat: Stat = Stat.ZERO) :
    BasicWeapon(weaponType, might, extraStat) {
    override val effectiveAgainstMoveType: Set<MoveType> = setOf(moveType)

    override val foeEffect: CombatStartSkill<Skill?>? = {
        if (it.foe.moveType == moveType) {
            FOE_EFFECT
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