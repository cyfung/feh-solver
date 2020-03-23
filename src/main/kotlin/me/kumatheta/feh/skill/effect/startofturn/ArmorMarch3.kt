package me.kumatheta.feh.skill.effect.startofturn

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.MoveType
import me.kumatheta.feh.PositiveStatus
import me.kumatheta.feh.skill.adjacentAllies
import me.kumatheta.feh.skill.effect.StartOfTurnEffect

object ArmorMarch3: StartOfTurnEffect {
    override fun onStartOfTurn(battleState: BattleState, self: HeroUnit) {
        val armorAllies = self.adjacentAllies(battleState).filter { it.moveType == MoveType.ARMORED }.toList()
        if (armorAllies.isNotEmpty()) {
            self.addPositiveStatus(PositiveStatus.EXTRA_TRAVEL_POWER)
            armorAllies.forEach {
                it.addPositiveStatus(PositiveStatus.EXTRA_TRAVEL_POWER)
            }
        }
    }
}