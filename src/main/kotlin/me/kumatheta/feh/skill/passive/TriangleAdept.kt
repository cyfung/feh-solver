package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.InCombatSkill
import me.kumatheta.feh.skill.Passive
import me.kumatheta.feh.skill.combatSkill

object TriangleAdept3 : BasicSkill() {
    override val triangleAdept: InCombatSkill<Int>? =
        combatSkill(20)
}