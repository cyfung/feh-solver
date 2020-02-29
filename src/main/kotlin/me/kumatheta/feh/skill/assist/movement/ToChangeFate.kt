package me.kumatheta.feh.skill.assist.movement

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.NegativeStatus
import me.kumatheta.feh.Position
import me.kumatheta.feh.skill.ProtectiveMovementAssist
import me.kumatheta.feh.Stat

private val BUFF = Stat(atk = 6)

object ToChangeFate : ProtectiveMovementAssist(true, RepositionEffect) {
    override fun onMove(self: HeroUnit, target: HeroUnit, battleState: BattleState) {
        self.applyBuff(BUFF)
        self.addNegativeStatus(NegativeStatus.ISOLATION)
    }
}