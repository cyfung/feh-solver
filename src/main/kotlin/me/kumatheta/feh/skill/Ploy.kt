package me.kumatheta.feh.skill

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.MapSkillMethod
import me.kumatheta.feh.foe

class Ploy(
    private val condition: (self: HeroUnit, foe: HeroUnit) -> Boolean,
    private val action: (HeroUnit) -> Unit
) : MapSkillMethod<Unit> {
    override fun apply(battleState: BattleState, self: HeroUnit) {
        battleState.unitsSeq(self.team.foe).filter {
            it.position.x == self.position.x || it.position.y == self.position.y
        }.filter {
            condition(self, it)
        }.forEach(action)
    }
}