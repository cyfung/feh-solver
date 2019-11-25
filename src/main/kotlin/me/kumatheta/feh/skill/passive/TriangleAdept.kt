package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.ConstantInCombatSkill
import me.kumatheta.feh.InCombatSkill
import me.kumatheta.feh.Passive

object TriangleAdept3 : Passive {
    override val triangleAdept: InCombatSkill<Int>? = ConstantInCombatSkill(20)
}