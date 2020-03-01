package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.*
import me.kumatheta.feh.skill.MapSkillMethod
import me.kumatheta.feh.skill.adjacentAllies

fun armorMarch3(): MapSkillMethod<Unit> = { battleState: BattleState, self: HeroUnit ->
    val armorAllies = self.adjacentAllies(battleState).filter { it.moveType == MoveType.ARMORED }.toList()
    if (armorAllies.isNotEmpty()) {
        self.addPositiveStatus(PositiveStatus.EXTRA_TRAVEL_POWER)
        armorAllies.forEach {
            it.addPositiveStatus(PositiveStatus.EXTRA_TRAVEL_POWER)
        }
    }

}
