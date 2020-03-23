package me.kumatheta.feh.skill.effect.teleport

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.MoveType
import me.kumatheta.feh.skill.effect.GuidanceEffect

object Guidance3 : GuidanceEffect {
    override fun canTeleportNextTo(battleState: BattleState, self: HeroUnit, target: HeroUnit): Boolean {
        return (target.moveType == MoveType.INFANTRY || target.moveType == MoveType.ARMORED) &&
                target.position.distanceTo(self.position) <= 2
    }
}
