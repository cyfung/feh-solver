package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.InCombatSkill
import me.kumatheta.feh.Passive
import me.kumatheta.feh.combatSkill

object TriangleAdept3 : Passive {
    override val triangleAdept: InCombatSkill<Int>? = combatSkill(20)
}