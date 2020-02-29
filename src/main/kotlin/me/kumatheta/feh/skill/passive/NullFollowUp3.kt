package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.CombatStartSkill
import me.kumatheta.feh.InCombatSkill
import me.kumatheta.feh.Passive
import me.kumatheta.feh.combatStartSkillTrue

object NullFollowUp3 : Passive {
    override val neutralizeFollowUp: CombatStartSkill<Boolean>? = combatStartSkillTrue
}