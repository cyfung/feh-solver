package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.MoveType
import me.kumatheta.feh.Passive

object IoteShield : Passive {
    override val neutralizeEffectiveMoveType: Set<MoveType>? = setOf(MoveType.FLYING)
}