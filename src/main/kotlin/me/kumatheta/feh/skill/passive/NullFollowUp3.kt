package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.CombatStartSkill
import me.kumatheta.feh.skill.Passive
import me.kumatheta.feh.skill.combatStartSkillTrue

object NullFollowUp3 : BasicSkill() {
    override val neutralizeFollowUp: CombatStartSkill<Boolean>? =
        combatStartSkillTrue
}