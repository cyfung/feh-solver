package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.*

private val ZERO = BasicSkill(neutralizeBonus = combatStartSkill(Stat.ZERO))

object DullClose3 : BasicSkill() {
    override val foeEffect: CombatStartSkill<Skill?>? = {
        if (it.foe.weaponType.isRanged) {
            null
        } else {
            ZERO
        }
    }
}

object DullRange3 : BasicSkill() {
    override val foeEffect: CombatStartSkill<Skill?>? = {
        if (it.foe.weaponType.isRanged) {
            ZERO
        } else {
            null
        }
    }
}