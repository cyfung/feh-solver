package me.kumatheta.feh.skill.assist

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.allies
import me.kumatheta.feh.skill.inCardinalDirection

private val BUFF = Stat(atk = 3, spd = 3, def = 3, res = 3)

object GentleDream : Refresh() {
    override fun apply(
        self: HeroUnit,
        target: HeroUnit,
        battleState: BattleState
    ) {
        super.apply(self, target, battleState)
        self.allies(battleState).filter {
            target == it || self.inCardinalDirection(it) || target.inCardinalDirection(it)
        }.forEach {
            it.applyBuff(BUFF)
        }
    }
}