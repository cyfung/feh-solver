package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.MoveType
import me.kumatheta.feh.skill.BasicSkill

val Guidance3 = BasicSkill(
    guidance = { battleState: BattleState, self: HeroUnit, target: HeroUnit ->
        (target.moveType == MoveType.INFANTRY || target.moveType == MoveType.ARMORED) &&
                target.position.distanceTo(self.position) <= 2
    }
)
