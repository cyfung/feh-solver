package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.MoveType
import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.Passive

object IoteShield : BasicSkill() {
    override val neutralizeEffectiveMoveType: Set<MoveType>? = setOf(MoveType.FLYING)
}