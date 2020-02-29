package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.*
import me.kumatheta.feh.skill.effect.negativeStatus
import me.kumatheta.feh.skill.toCounterPassive

private val FOE_EFFECT: CombatStartSkill<Skill?> = {
    combatStartSkill(-1).toCounterPassive()
}

class TrilemmaPlus(disableCounter: Boolean, staffAsNormal: Boolean) : BasicWeapon(Staff, 12) {
    override val specialDebuff: SpecialDebuff? = SpecialDebuff.ALWAYS_AVAILABLE
    override val combatEnd: CombatEndSkill? = negativeStatus(NegativeStatus.TRIANGLE, 2, true)

    override val foeEffect: CombatStartSkill<Skill?>? = if (disableCounter) {
        FOE_EFFECT
    } else {
        null
    }
    override val staffAsNormal: InCombatSkill<Boolean>? = if (staffAsNormal) {
        inCombatSkillTrue
    } else {
        null
    }
}